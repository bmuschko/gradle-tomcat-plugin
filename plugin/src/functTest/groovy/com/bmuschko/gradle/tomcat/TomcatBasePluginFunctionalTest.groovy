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
import spock.lang.Unroll

class TomcatBasePluginFunctionalTest extends AbstractFunctionalTest {
    def setup() {
        buildFile << """
            plugins {
                id 'com.bmuschko.tomcat-base'
            }
        """
        buildFile << basicBuildScript()
    }

    @Unroll
    def "Start and stop #tomcatVersion with #taskName without supporting web app directory"() {
        setup:
        buildFile << """
            task myTomcatRun(type: com.bmuschko.gradle.tomcat.tasks.TomcatRun) {
                httpPort = $httpPort
                stopPort = $stopPort
                daemon = true
                user 'user1', '1234567', ['developer', 'admin']
                user 'user2', 'abcdef', ['manager']
            }

            task myTomcatRunWar(type: com.bmuschko.gradle.tomcat.tasks.TomcatRunWar) {
                httpPort = $httpPort
                stopPort = $stopPort
                daemon = true
                user 'user1', '1234567', ['developer', 'admin']
                user 'user2', 'abcdef', ['manager']
            }

            task myTomcatStop(type: com.bmuschko.gradle.tomcat.tasks.TomcatStop) {
                stopPort = $stopPort
            }
        """

        expect:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << getTomcatContainerLifecycleManagementBuildFileContent(taskName, 'myTomcatStop')
        build('startAndStopTomcat')

        where:
        tomcatVersion             | taskName
        TomcatVersion.VERSION_6_0_X  | 'myTomcatRun'
        TomcatVersion.VERSION_6_0_X  | 'myTomcatRunWar'
        TomcatVersion.VERSION_7_0_X  | 'myTomcatRun'
        TomcatVersion.VERSION_7_0_X  | 'myTomcatRunWar'
    }
}
