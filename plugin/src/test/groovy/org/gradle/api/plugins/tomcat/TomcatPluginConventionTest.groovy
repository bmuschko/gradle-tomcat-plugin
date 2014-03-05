/*
 * Copyright 2010 the original author or authors.
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

import spock.lang.Specification

/**
 * Test case for TomcatPluginConvention.
 *
 * @author Benjamin Muschko
 */
class TomcatPluginConventionTest extends Specification {
    TomcatPluginConvention pluginConvention = new TomcatPluginConvention()

    def "Verify default convention values"() {
        pluginConvention.httpPort == 8080
        pluginConvention.httpsPort == 8443
        pluginConvention.stopPort == 8081
        pluginConvention.ajpPort == 8009
        pluginConvention.stopKey == 'stopKey'
        pluginConvention.httpProtocol == TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        pluginConvention.httpsProtocol == TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        pluginConvention.ajpProtocol == TomcatPluginConvention.DEFAULT_AJP_PROTOCOL_HANDLER
    }
}
