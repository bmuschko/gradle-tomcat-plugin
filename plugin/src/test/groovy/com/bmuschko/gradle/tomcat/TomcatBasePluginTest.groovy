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

import com.bmuschko.gradle.tomcat.tasks.AbstractTomcatRun
import com.bmuschko.gradle.tomcat.tasks.Tomcat
import com.bmuschko.gradle.tomcat.tasks.TomcatJasper
import com.bmuschko.gradle.tomcat.tasks.TomcatRun
import com.bmuschko.gradle.tomcat.tasks.TomcatRunWar
import com.bmuschko.gradle.tomcat.tasks.TomcatStop
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.WarPlugin
import com.bmuschko.gradle.tomcat.embedded.TomcatUser
import com.bmuschko.gradle.tomcat.extension.TomcatPluginExtension
import org.gradle.api.plugins.tomcat.tasks.*
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test case for Tomcat base plugin.
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
            TomcatRun task = project.task('myTomcatRun', type: TomcatRun)
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
            task.users.size() == 0
    }

    def "Can create task of type TomcatRunWar"() {
        expect:
            TomcatPluginExtension extension = new TomcatPluginExtension()
            TomcatRunWar task = project.task('myTomcatRunWar', type: TomcatRunWar)
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
            task.users.size() == 0
    }

    def "Can create task of type TomcatStop"() {
        expect:
            TomcatPluginExtension extension = new TomcatPluginExtension()
            TomcatStop task = project.task('myTomcatStop', type: TomcatStop)
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

    @Unroll
    def "Can add users to task of type #taskType with convenience method"() {
        when:
            AbstractTomcatRun task = project.task('myTomcatTask', type: taskType)
            task.user 'user1', '1234567', ['developer', 'admin']
            task.user 'user2', 'abcdef', ['manager']
        then:
            task.users.size() == 2
            task.users[0] == new TomcatUser(username: 'user1', password: '1234567', roles: ['developer', 'admin'])
            task.users[1] == new TomcatUser(username: 'user2', password: 'abcdef', roles: ['manager'])
        where:
            taskType << [TomcatRun, TomcatRunWar]
    }
}
