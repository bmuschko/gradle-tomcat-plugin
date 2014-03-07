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
package org.gradle.api.plugins.tomcat

import org.gradle.api.plugins.tomcat.embedded.TomcatVersion
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.Task
import spock.lang.Ignore
import spock.lang.Unroll

class TomcatPluginIntegrationTest extends AbstractIntegrationTest {
    def setup() {
        buildFile << """
apply plugin: org.gradle.api.plugins.tomcat.TomcatPlugin

[tomcatRun, tomcatRunWar]*.daemon = true
"""
    }

    def "Adds default Tomcat tasks for Java project"() {
        when:
            GradleProject project = runTasks(integTestDir, 'tasks')
        then:
            Task tomcatRunTask = findTask(project, TomcatPlugin.TOMCAT_RUN_TASK_NAME)
            tomcatRunTask
            tomcatRunTask.description == 'Uses your files as and where they are and deploys them to Tomcat.'
            Task tomcatRunWarTask = findTask(project, TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME)
            tomcatRunWarTask
            tomcatRunWarTask.description == 'Assembles the webapp into a war and deploys it to Tomcat.'
            Task tomcatStopTask = findTask(project, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
            tomcatStopTask
            tomcatStopTask.description == 'Stops Tomcat.'
            Task tomcatJasperTask = findTask(project, TomcatPlugin.TOMCAT_JASPER_TASK_NAME)
            tomcatJasperTask
            tomcatJasperTask.description == 'Runs the JSP compiler and turns JSP pages into Java source.'
    }

    @Unroll
    def "Start and stop #tomcatVersion with #taskName supporting default web app directory"() {
        setup:
            setupWebAppDirectory()

        expect:
            buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
            buildFile << getTaskStartAndStopProperties()
            buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
            runTasks(integTestDir, 'startAndStopTomcat')

        where:
            tomcatVersion             | taskName
            TomcatVersion.VERSION_6X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
            TomcatVersion.VERSION_6X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
            TomcatVersion.VERSION_7X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
            TomcatVersion.VERSION_7X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
    }

    @Unroll
    def "Start and stop #tomcatVersion with #taskName without supporting web app directory"() {
        expect:
            buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
            buildFile << getTaskStartAndStopProperties()
            buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
            runTasks(integTestDir, 'startAndStopTomcat')

        where:
            tomcatVersion             | taskName
            TomcatVersion.VERSION_6X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
            TomcatVersion.VERSION_6X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
            TomcatVersion.VERSION_7X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
            TomcatVersion.VERSION_7X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
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
}
"""
        runTasks(integTestDir, 'startAndStopTomcat')

        where:
            tomcatVersion             | taskName
            TomcatVersion.VERSION_6X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
            TomcatVersion.VERSION_6X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
            TomcatVersion.VERSION_7X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
            TomcatVersion.VERSION_7X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
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
        runTasks(integTestDir, 'startAndStopTomcat')
        new File(integTestDir, 'logs/tomcat.log').exists()
        !new File(integTestDir, 'logs/tomcat.log.lck').exists()

        where:
            tomcatVersion             | taskName
            TomcatVersion.VERSION_6X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
            TomcatVersion.VERSION_6X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
            TomcatVersion.VERSION_7X  | TomcatPlugin.TOMCAT_RUN_TASK_NAME
            TomcatVersion.VERSION_7X  | TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME
    }

    @Ignore
    def "Start and stop Tomcat 8x with tomcatRun task supporting default web app directory"() {
        setup:
            setupWebAppDirectory()

        expect:
            buildFile << getBasicTomcatBuildFileContent(TomcatVersion.VERSION_8X)
            buildFile << getTaskStartAndStopProperties()
            buildFile << getTomcatContainerLifecycleManagementBuildFileContent(TomcatPlugin.TOMCAT_RUN_TASK_NAME, TomcatPlugin.TOMCAT_STOP_TASK_NAME)
            runTasks(integTestDir, 'startAndStopTomcat')
    }

    private void getTaskStartAndStopProperties() {
        buildFile << """
[tomcatRun, tomcatRunWar]*.httpPort = $httpPort
[tomcatRun, tomcatRunWar, tomcatStop]*.stopKey = 'stopKey'
[tomcatRun, tomcatRunWar, tomcatStop]*.stopPort = $stopPort
"""
    }
}
