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

import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test case for TomcatPluginConvention.
 *
 * @author Benjamin Muschko
 */
class TomcatPluginConventionTest {
    private TomcatPluginConvention pluginConvention

    @Before
    void setUp() {
        pluginConvention = new TomcatPluginConvention()
    }

    @After
    void tearDown() {
        pluginConvention = null  
    }

    @Test
    void testGetHttpPortForDefaultValue() {
        assert pluginConvention.httpPort == 8080
    }

    @Test
    void testGetStopPortForDefaultValue() {
        assert pluginConvention.stopPort == 8081
    }

    @Test
    void testGetStopKeyForDefaultValue() {
        assert pluginConvention.stopKey == null
    }
}
