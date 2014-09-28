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
import spock.lang.Unroll

class TomcatBasePluginIntegrationTest extends AbstractIntegrationTest {
    def setup() {
        buildFile << """
apply plugin: org.gradle.api.plugins.tomcat.TomcatBasePlugin
"""
    }

    @Unroll
    def "Start and stop #tomcatVersion with #taskName without supporting web app directory"() {
        setup:
            buildFile << """
task myTomcatRun(type: org.gradle.api.plugins.tomcat.tasks.TomcatRun) {
    httpPort = $httpPort
    stopPort = $stopPort
    daemon = true
    user 'user1', '1234567', ['developer', 'admin']
    user 'user2', 'abcdef', ['manager']
}

task myTomcatRunWar(type: org.gradle.api.plugins.tomcat.tasks.TomcatRunWar) {
    httpPort = $httpPort
    stopPort = $stopPort
    daemon = true
    user 'user1', '1234567', ['developer', 'admin']
    user 'user2', 'abcdef', ['manager']
}

task myTomcatStop(type: org.gradle.api.plugins.tomcat.tasks.TomcatStop) {
    stopPort = $stopPort
}
"""
        expect:
            buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
            buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, 'myTomcatStop')
            runTasks(integTestDir, 'startAndStopTomcat')

        where:
            tomcatVersion             | taskName
            TomcatVersion.VERSION_6X  | 'myTomcatRun'
            TomcatVersion.VERSION_6X  | 'myTomcatRunWar'
            TomcatVersion.VERSION_7X  | 'myTomcatRun'
            TomcatVersion.VERSION_7X  | 'myTomcatRunWar'
    }
}
