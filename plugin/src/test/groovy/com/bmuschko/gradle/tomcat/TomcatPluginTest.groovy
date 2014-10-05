/*
 * Copyright 2011 the original author or authors.
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

import org.gradle.api.Project
import org.gradle.api.plugins.WarPlugin
import com.bmuschko.gradle.tomcat.extension.TomcatPluginExtension
import com.bmuschko.gradle.tomcat.embedded.TomcatUser
import com.bmuschko.gradle.tomcat.tasks.TomcatJasper
import com.bmuschko.gradle.tomcat.tasks.TomcatRun
import com.bmuschko.gradle.tomcat.tasks.TomcatRunWar
import com.bmuschko.gradle.tomcat.tasks.TomcatStop
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Test case for Tomcat plugin.
 *
 * @author Benjamin Muschko
 */
class TomcatPluginTest extends Specification {
    private Project project

    def setup() {
        project = ProjectBuilder.builder().build()
        project.apply(plugin: TomcatPlugin)
    }

    def "Creates basic setup"() {
        expect:
            project.plugins.hasPlugin(WarPlugin)
            project.plugins.hasPlugin(TomcatBasePlugin)
            project.extensions.getByName(TomcatPlugin.TOMCAT_EXTENSION_NAME) instanceof TomcatPluginExtension
    }

    def "Creates and preconfigures tomcatRun task"() {
        expect:
            TomcatPluginExtension extension = project.extensions.getByName(TomcatPlugin.TOMCAT_EXTENSION_NAME)
            TomcatRun task = project.tasks.getByName(TomcatPlugin.TOMCAT_RUN_TASK_NAME)
            task instanceof TomcatRun
            task.description == 'Uses your files as and where they are and deploys them to Tomcat.'
            task.group == WarPlugin.WEB_APP_GROUP
            task.contextPath == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName
            task.httpPort == extension.httpPort
            task.httpsPort == extension.httpsPort
            task.stopPort == extension.stopPort
            task.stopKey == extension.stopKey
            task.enableSSL == extension.enableSSL
            !task.daemon
            task.reloadable
            task.webAppClasspath == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).classpath
            !task.webAppSourceDirectory
            task.ajpPort == 8009
            task.ajpProtocol == 'org.apache.coyote.ajp.AjpProtocol'
    }

    def "Creates and preconfigures tomcatRunWar task"() {
        expect:
            TomcatPluginExtension extension = project.extensions.getByName(TomcatPlugin.TOMCAT_EXTENSION_NAME)
            TomcatRunWar task = project.tasks.getByName(TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME)
            task instanceof TomcatRunWar
            task.description == 'Assembles the webapp into a war and deploys it to Tomcat.'
            task.group == WarPlugin.WEB_APP_GROUP

            task.contextPath == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName
            task.httpPort == extension.httpPort
            task.httpsPort == extension.httpsPort
            task.stopPort == extension.stopPort
            task.stopKey == extension.stopKey
            task.enableSSL == extension.enableSSL
            !task.daemon
            task.reloadable
            task.webApp == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).archivePath
    }

    def "Creates and preconfigures tomcatStop task"() {
        expect:
            TomcatPluginExtension extension = project.extensions.getByName(TomcatPlugin.TOMCAT_EXTENSION_NAME)
            TomcatStop task = project.tasks.getByName(TomcatPlugin.TOMCAT_STOP_TASK_NAME)
            task instanceof TomcatStop
            task.description == 'Stops Tomcat.'
            task.group == WarPlugin.WEB_APP_GROUP
            task.stopPort == extension.stopPort
            task.stopKey == extension.stopKey
    }

    def "Creates and preconfigures tomcatJasper task"() {
        expect:
            TomcatJasper task = project.tasks.getByName(TomcatPlugin.TOMCAT_JASPER_TASK_NAME)
            task instanceof TomcatJasper
            task.description == 'Runs the JSP compiler and turns JSP pages into Java source.'
            task.group == WarPlugin.WEB_APP_GROUP
            task.classpath
            !task.validateXml
            task.uriroot == project.webAppDir
            !task.webXmlFragment
            !task.addWebXmlMappings
            task.outputDir == new File(project.buildDir, 'jasper')
            task.classdebuginfo
            !task.compiler
            task.compilerSourceVM == '1.6'
            task.compilerTargetVM == '1.6'
            task.poolingEnabled
            task.errorOnUseBeanInvalidClassAttribute
            !task.genStringAsCharArray
            task.ieClassId == 'clsid:8AD9C840-044E-11D1-B3E9-00805F499D93'
            task.javaEncoding == 'UTF8'
            !task.trimSpaces
            !task.xpoweredBy
    }

    def "Can configure tasks with extension"() {
        when:
            project.tomcat {
                httpPort = 9090
                httpsPort = 9443
                stopPort = 9081
                ajpPort = 9009
                stopKey = 'myStopKey'
                enableSSL = true
                httpProtocol = 'org.apache.coyote.http11.Http11NioProtocol'
                httpsProtocol = 'org.apache.coyote.http11.Http11AprProtocol'
                ajpProtocol = 'org.apache.coyote.ajp.RandomAjpProtocol'

                users {
                    user {
                        username = 'user1'
                        password = 'pwd1'
                        roles = ['developer', 'admin']
                    }

                    user {
                        username = 'user2'
                        password = 'pwd2'
                        roles = ['manager']
                    }
                }
            }
        then:
            TomcatRun tomcatRunTask = project.tasks.getByName(TomcatPlugin.TOMCAT_RUN_TASK_NAME)
            tomcatRunTask.httpPort == 9090
            tomcatRunTask.httpsPort == 9443
            tomcatRunTask.stopPort == 9081
            tomcatRunTask.ajpPort == 9009
            tomcatRunTask.stopKey == 'myStopKey'
            tomcatRunTask.enableSSL == true
            tomcatRunTask.httpProtocol == 'org.apache.coyote.http11.Http11NioProtocol'
            tomcatRunTask.httpsProtocol == 'org.apache.coyote.http11.Http11AprProtocol'
            tomcatRunTask.ajpProtocol == 'org.apache.coyote.ajp.RandomAjpProtocol'
            tomcatRunTask.users.size() == 2
            TomcatUser tomcatUser1 = tomcatRunTask.users[0]
            tomcatUser1.username == 'user1'
            tomcatUser1.password == 'pwd1'
            tomcatUser1.roles[0] == 'developer'
            tomcatUser1.roles[1] == 'admin'
            TomcatUser tomcatUser2 = tomcatRunTask.users[1]
            tomcatUser2.username == 'user2'
            tomcatUser2.password == 'pwd2'
            tomcatUser2.roles[0] == 'manager'
            TomcatRunWar tomcatRunWarTask = project.tasks.getByName(TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME)
            tomcatRunWarTask.httpPort == 9090
            tomcatRunWarTask.httpsPort == 9443
            tomcatRunWarTask.stopPort == 9081
            tomcatRunWarTask.ajpPort == 9009
            tomcatRunWarTask.stopKey == 'myStopKey'
            tomcatRunWarTask.enableSSL == true
            tomcatRunWarTask.httpProtocol == 'org.apache.coyote.http11.Http11NioProtocol'
            tomcatRunWarTask.httpsProtocol == 'org.apache.coyote.http11.Http11AprProtocol'
            tomcatRunWarTask.ajpProtocol == 'org.apache.coyote.ajp.RandomAjpProtocol'
            TomcatStop tomcatStopTask = project.tasks.getByName(TomcatPlugin.TOMCAT_STOP_TASK_NAME)
            tomcatStopTask.stopPort == 9081
            tomcatStopTask.stopKey == 'myStopKey'
    }
}
