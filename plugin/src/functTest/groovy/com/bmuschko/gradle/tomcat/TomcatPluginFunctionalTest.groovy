/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmuschko.gradle.tomcat

import com.bmuschko.gradle.tomcat.embedded.TomcatVersion
import org.gradle.testkit.runner.BuildResult
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Unroll

class TomcatPluginFunctionalTest extends AbstractFunctionalTest {
    final WebComponentFixture webComponentFixture = new WebComponentFixture()

    def setup() {
        buildFile << """
            plugins {
                id 'com.bmuschko.tomcat'
            }

            [tomcatRun, tomcatRunWar]*.daemon = true
        """
        buildFile << basicBuildScript()
    }

    def "Adds default Tomcat tasks for Java project"() {
        when:
        BuildResult buildResult = build('tasks')

        then:
        buildResult.output.contains("""Web application tasks
---------------------
tomcatJasper - Runs the JSP compiler and turns JSP pages into Java source.
tomcatRun - Uses your files as and where they are and deploys them to Tomcat.
tomcatRunWar - Assembles the webapp into a war and deploys it to Tomcat.
tomcatStop - Stops Tomcat.""")
    }

    @Unroll
    def "Start and stop #tomcatVersion with #taskName supporting default web app directory"() {
        setup:
        setupWebAppDirectory()

        expect:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        build('startAndStopTomcat')

        where:
        tomcatVersion                | taskName
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
        TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
        TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
    }

    @Unroll
    def "Start and stop #tomcatVersion with #taskName for servlet context listener"() {
        setup:
        setupWebAppDirectory()
        webComponentFixture.createServletContextListenerWebApp(temporaryFolder.root)

        when:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        buildFile << """
            dependencies {
                providedCompile 'javax.servlet:javax.servlet-api:3.0.1'
            }

            tomcatStop.doLast {
                println 'tomcatStop.doLast'
            }
        """
        BuildResult buildResult = build('startAndStopTomcat')

        then:
        buildResult.output.contains('contextInitialized')
        buildResult.output.contains('contextDestroyed')
        buildResult.output.contains('tomcatStop.doLast')

        where:
        tomcatVersion               | taskName
        TomcatVersion.VERSION_7_0_X | TomcatPlugin.TOMCAT_RUN_TASK_NAME
    }

    @Unroll
    def "Start and stop #tomcatVersion with #taskName for Servlet 2.x web app"() {
        setup:
        setupWebAppDirectory()
        webComponentFixture.createServlet2xWebApp(temporaryFolder.root)

        expect:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        buildFile << """
            dependencies {
                providedCompile 'javax.servlet:servlet-api:2.5'
            }

            startAndStopTomcat.doLast {
                assert 'http://localhost:$httpPort/$temporaryFolder.root.name/simple'.toURL().text == 'Hello World!'
                assert 'http://localhost:$httpPort/$temporaryFolder.root.name/forward'.toURL().text == 'Forward successful!'
            }
        """
        build('startAndStopTomcat')

        where:
        tomcatVersion                | taskName
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
        TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME

        // TODO: Not sure why that fails
        //TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
    }

    @Unroll
    def "Start and stop #tomcatVersion with #taskName without supporting web app directory"() {
        expect:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        build('startAndStopTomcat')

        where:
        tomcatVersion                | taskName
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
        TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
        TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
    }

    @Unroll
    def "Start and stop #tomcatVersion with #taskName configured by extension"() {
        expect:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        buildFile << """
            tomcat {
                httpPort = $httpPort
                stopKey = 'stopThis'
                stopPort = $stopPort

                users {
                    user {
                        username = 'user1'
                        password = '123456'
                        roles = ['developers', 'admin']
                    }

                    user {
                        username = 'user2'
                        password = 'abcdef'
                        roles = ['manager']
                    }
                }
            }
        """
        build('startAndStopTomcat')

        where:
        tomcatVersion                | taskName
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
        TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
        TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
    }

    @Unroll
    def "Running #taskName task redirecting logs to file and cleans up resources after stopping Tomcat in daemon mode"() {
        expect:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        buildFile << """
            [tomcatRun, tomcatRunWar]*.outputFile = file('logs/tomcat.log')
        """
        build('startAndStopTomcat')
        new File(temporaryFolder.root, 'logs/tomcat.log').exists()
        !new File(temporaryFolder.root, 'logs/tomcat.log.lck').exists()

        where:
        tomcatVersion                | taskName
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
        TomcatVersion.VERSION_6_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
        TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
        TomcatVersion.VERSION_7_0_X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
    }

    @Ignore
    def "Start and stop Tomcat 8.0.x with tomcatRun task supporting default web app directory"() {
        setup:
        setupWebAppDirectory()

        expect:
        buildFile << getBasicTomcatBuildFileContent(TomcatVersion.VERSION_8_0_X)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(TomcatPlugin.TOMCAT_RUN_TASK_NAME, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        build('startAndStopTomcat')
    }

    private void getTaskStartAndStopProperties() {
        buildFile << """
            [tomcatRun, tomcatRunWar]*.httpPort = $httpPort
            [tomcatRun, tomcatRunWar]*.ajpPort = $ajpPort
            [tomcatRun, tomcatRunWar, tomcatStop]*.stopKey = 'stopKey'
            [tomcatRun, tomcatRunWar, tomcatStop]*.stopPort = $stopPort
        """
    }

    @Ignore
    def "Start and stop Tomcat 8.5.x with tomcatRun task supporting default web app directory"() {
        setup:
        setupWebAppDirectory()

        expect:
        buildFile << getBasicTomcatBuildFileContent(TomcatVersion.VERSION_8_5_X)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(TomcatPlugin.TOMCAT_RUN_TASK_NAME, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        build('startAndStopTomcat')
    }

    @Ignore
    def "Start and stop Tomcat 9.0.x with tomcatRun task supporting default web app directory"() {
        setup:
        setupWebAppDirectory()

        expect:
        buildFile << getBasicTomcatBuildFileContent(TomcatVersion.VERSION_9_0_X)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(TomcatPlugin.TOMCAT_RUN_TASK_NAME, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        build('startAndStopTomcat')
    }

    @Unroll
    def "Fails to execute tomcatRun with #tomcatVersion for non-existent config file"() {
        setup:
        setupWebAppDirectory()

        when:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(TomcatPlugin.TOMCAT_RUN_TASK_NAME, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        File configFile = new File(temporaryFolder.root, 'config/myconfig.xml')
        buildFile << """
            tomcatRun.configFile = new File('$configFile.canonicalPath')
        """
        BuildResult result = buildAndFail('startAndStopTomcat')

        then:
        result.output.contains("File '$configFile.canonicalPath' specified for property 'configFile' does not exist.")

        where:
        tomcatVersion << [TomcatVersion.VERSION_6_0_X, TomcatVersion.VERSION_7_0_X]
    }

    @Unroll
    @Issue("https://github.com/bmuschko/gradle-tomcat-plugin/issues/45")
    def "Isolates classpath with #tomcatVersion from Gradle core classpath"() {
        given:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTaskStartAndStopProperties()
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(TomcatPlugin.TOMCAT_RUN_TASK_NAME, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        buildFile << """
            dependencies {
                compile 'ch.qos.logback:logback-classic:1.1.2'
            }
        """
        when:
        BuildResult result = build('startAndStopTomcat')

        then:
        !result.output.contains('SLF4J: Class path contains multiple SLF4J bindings.')

        where:
        tomcatVersion << [TomcatVersion.VERSION_6_0_X, TomcatVersion.VERSION_7_0_X]
    }

    @Unroll
    def "Resolves config file for tomcatRun with Tomcat #tomcatVersion"() {
        setup:
        setupWebAppDirectory()
        File configFile = createConfigFile()
        File staticFile = createStaticFile()

        expect:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(TomcatPlugin.TOMCAT_RUN_TASK_NAME, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
        buildFile << getTaskStartAndStopProperties()
        buildFile << """
            tomcatRun.configFile = new File('$configFile.canonicalPath')

            startAndStopTomcat.doLast {
                assert 'http://localhost:$httpPort/$temporaryFolder.root.name/alt/$staticFile.name'.toURL().text == 'This is a test!'
            }
        """
        build('startAndStopTomcat')

        where:
        tomcatVersion << ['7.0.52']
    }

    private File createConfigFile() {
        File configDir = temporaryFolder.newFolder('config')
        File configFile = createFile(configDir, 'myconfig.xml')
        configFile << """<?xml version="1.0" encoding="UTF-8"?>
            <Context>
                <Resources className="org.apache.naming.resources.VirtualDirContext" extraResourcePaths="/alt=$temporaryFolder.root.canonicalPath/static" />
            </Context>
        """
        configFile
    }

    private File createStaticFile() {
        File staticDir =  temporaryFolder.newFolder('static')
        File testHtmlFile = createFile(staticDir, 'test.html')
        testHtmlFile << 'This is a test!'
    }
}
