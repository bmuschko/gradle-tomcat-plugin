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

import org.gradle.api.InvalidUserDataException
import org.junit.After
import org.junit.Test

/**
 * Test case for TomcatSystemPropertyTest. 
 *
 * @author Benjamin Muschko
 */
class TomcatSystemPropertyTest {
    @After
    void tearDown() {
        System.clearProperty(TomcatSystemProperty.HTTP_PORT_SYSPROPERTY)
        System.clearProperty(TomcatSystemProperty.STOP_PORT_SYSPROPERTY)
        System.clearProperty(TomcatSystemProperty.STOP_KEY_SYSPROPERTY)
    }

    @Test
    void testGetHttpPortForNonExistingProperty() {
        assert TomcatSystemProperty.getHttpPort() == null
    }

    @Test(expected = InvalidUserDataException.class)
    void testGetHttpPortForInvalidExistingProperty() {
        System.setProperty(TomcatSystemProperty.HTTP_PORT_SYSPROPERTY, "xxx")
        TomcatSystemProperty.getHttpPort()
    }

    @Test
    void testGetHttpPortForValidExistingProperty() {
        System.setProperty(TomcatSystemProperty.HTTP_PORT_SYSPROPERTY, "1234")
        assert TomcatSystemProperty.getHttpPort() == 1234
    }

    @Test
    void testGetHttpsPortForNonExistingProperty() {
        assert TomcatSystemProperty.getHttpsPort() == null
    }

    @Test(expected = InvalidUserDataException.class)
    void testGetHttpsPortForInvalidExistingProperty() {
        System.setProperty(TomcatSystemProperty.HTTPS_PORT_SYSPROPERTY, "xxx")
        TomcatSystemProperty.getHttpsPort()
    }

    @Test
    void testGetHttpsPortForValidExistingProperty() {
        System.setProperty(TomcatSystemProperty.HTTPS_PORT_SYSPROPERTY, "1234")
        assert TomcatSystemProperty.getHttpsPort() == 1234
    }

    @Test
    void testGetStopPortForNonExistingProperty() {
        assert TomcatSystemProperty.getStopPort() == null
    }

    @Test(expected = InvalidUserDataException.class)
    void testGetStopPortForInvalidExistingProperty() {
        System.setProperty(TomcatSystemProperty.STOP_PORT_SYSPROPERTY, "xxx")
        TomcatSystemProperty.getStopPort()
    }

    @Test
    void testGetStopPortForValidExistingProperty() {
        System.setProperty(TomcatSystemProperty.STOP_PORT_SYSPROPERTY, "1234")
        assert TomcatSystemProperty.getStopPort() == 1234
    }

    @Test
    void testGetStopKeyForNonExistingProperty() {
        assert TomcatSystemProperty.getStopKey() == null
    }

    @Test
    void testGetStopKeyForValidExistingProperty() {
        System.setProperty(TomcatSystemProperty.STOP_KEY_SYSPROPERTY, "X")
        assert TomcatSystemProperty.getStopKey() == "X"
    }

    @Test
    void testGetEnableSSLForNonExistingProperty() {
        assert TomcatSystemProperty.getEnableSSL() == null
    }

    @Test
    void testGetEnableSSLForValidExistingPropertyTrue() {
        System.setProperty(TomcatSystemProperty.ENABLE_SSL_SYSPROPERTY, "true")
        assert TomcatSystemProperty.getEnableSSL() == Boolean.TRUE
    }

    @Test
    void testGetEnableSSLForValidExistingPropertyFalse() {
        System.setProperty(TomcatSystemProperty.ENABLE_SSL_SYSPROPERTY, "false")
        assert TomcatSystemProperty.getEnableSSL() == Boolean.FALSE
    }
}
