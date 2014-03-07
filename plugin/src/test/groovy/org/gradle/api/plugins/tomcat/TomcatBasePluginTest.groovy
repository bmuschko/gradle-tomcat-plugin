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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.tomcat.extension.TomcatPluginExtension
import org.gradle.api.plugins.tomcat.tasks.*
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Test case for Tomcat base plugin.
 *
 * @author Benjamin Muschko
 */
class TomcatBasePluginTest extends Specification {
    private Project project

    def setup() {
        project = ProjectBuilder.builder().build()
        project.apply(plugin: TomcatBasePlugin)
    }

    def "Creates basic setup"() {
        expect:
            project.plugins.hasPlugin(WarPlugin)
            project.tasks.withType(Tomcat).size() == 0
    }

    def "Can create task of type TomcatRun"() {
        expect:
            TomcatPluginExtension extension = new TomcatPluginExtension()
            Task task = project.task('myTomcatRun', type: TomcatRun)
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

    def "Can create task of type TomcatRunWar"() {
        expect:
            TomcatPluginExtension extension = new TomcatPluginExtension()
            Task task = project.task('myTomcatRunWar', type: TomcatRunWar)
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

    def "Can create task of type TomcatStop"() {
        expect:
            TomcatPluginExtension extension = new TomcatPluginExtension()
            Task task = project.task('myTomcatStop', type: TomcatStop)
            task.group == WarPlugin.WEB_APP_GROUP
            task.stopPort == extension.stopPort
            task.stopKey == extension.stopKey
    }

    def "Creates and preconfigures tomcatJasper task"() {
        expect:
            Task task = project.task('myTomcatJaser', type: TomcatJasper)
            task.group == WarPlugin.WEB_APP_GROUP
            task.classpath
    }
}
