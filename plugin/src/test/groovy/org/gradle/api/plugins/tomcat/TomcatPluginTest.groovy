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
package org.gradle.api.plugins.tomcat

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.WarPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Test case for Tomcat plugin.
 *
 * @author Benjamin Muschko
 */
class TomcatPluginTest extends Specification {
    private final File testDir = new File("build/tmp/tests")
    private Project project
    private TomcatPlugin tomcatPlugin

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testDir).build()
        tomcatPlugin = new TomcatPlugin().apply(project)
    }

    def cleanup() {
        if(testDir.exists()) {
            testDir.deleteDir()
        }
    }

    def "Apply basic setup"() {
        expect:
            project.plugins.hasPlugin(WarPlugin)
            project.convention.plugins.tomcat instanceof TomcatPluginConvention
    }

    def "Apply tomcatRun task"() {
        expect:
            Task task = project.tasks.getByName(TomcatPlugin.TOMCAT_RUN_TASK_NAME)
            task instanceof TomcatRun
            task.description == 'Uses your files as and where they are and deploys them to Tomcat.'
            task.group == WarPlugin.WEB_APP_GROUP
            task.contextPath == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName
            task.httpPort == project.httpPort
            task.httpsPort == project.httpsPort
            task.stopPort == project.stopPort
            task.stopKey == project.stopKey
            task.enableSSL == project.enableSSL
            !task.daemon
            task.reloadable
            task.webAppClasspath == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).classpath
            !task.webAppSourceDirectory
            task.ajpPort == 8009
            task.ajpProtocol == 'org.apache.coyote.ajp.AjpProtocol'
    }

    def "Apply tomcatRunWar task"() {
        expect:
            Task task = project.tasks.getByName(TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME)
            task instanceof TomcatRunWar
            task.description == 'Assembles the webapp into a war and deploys it to Tomcat.'
            task.group == WarPlugin.WEB_APP_GROUP
            task.contextPath == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName
            task.httpPort == project.httpPort
            task.httpsPort == project.httpsPort
            task.stopPort == project.stopPort
            task.stopKey == project.stopKey
            task.enableSSL == project.enableSSL
            !task.daemon
            task.reloadable
            task.webApp == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).archivePath
    }

    def "Apply tomcatStop task"() {
        expect:
            Task task = project.tasks.getByName(TomcatPlugin.TOMCAT_STOP_TASK_NAME)
            task instanceof TomcatStop
            task.description == 'Stops Tomcat.'
            task.group == WarPlugin.WEB_APP_GROUP
            task.stopPort == project.stopPort
            task.stopKey == project.stopKey
    }

    def "Apply tomcatJasper task"() {
        expect:
            Task task = project.tasks.getByName(TomcatPlugin.TOMCAT_JASPER_TASK_NAME)
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
}
