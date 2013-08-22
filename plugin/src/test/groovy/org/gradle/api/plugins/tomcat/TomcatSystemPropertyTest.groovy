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
import org.junit.Before
import org.junit.Test

/**
 * Test case for TomcatSystemPropertyTest. 
 *
 * @author Benjamin Muschko
 */
class TomcatSystemPropertyTest {
    @Before
    void tearDown() {
        System.clearProperty(TomcatSystemProperty.HTTP_PORT_SYSPROPERTY)
        System.clearProperty(TomcatSystemProperty.HTTPS_PORT_SYSPROPERTY)
        System.clearProperty(TomcatSystemProperty.STOP_PORT_SYSPROPERTY)
        System.clearProperty(TomcatSystemProperty.STOP_KEY_SYSPROPERTY)
        System.clearProperty(TomcatSystemProperty.ENABLE_SSL_SYSPROPERTY)
        System.clearProperty(TomcatSystemProperty.HTTP_PROTOCOL_SYSPROPERTY)
        System.clearProperty(TomcatSystemProperty.HTTPS_PROTOCOL_SYSPROPERTY)
        System.clearProperty(TomcatSystemProperty.AJP_PORT_SYSPROPERTY);
        System.clearProperty(TomcatSystemProperty.AJP_PROTOCOL_SYSPROPERTY);
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

    @Test
    void testGetHttpProtocolHandlerClassNameForNonExistingProperty() {
        assert TomcatSystemProperty.getHttpProtocolHandlerClassName() == null
    }

    @Test
    void testGetHttpProtocolHandlerClassNameForValidExistingProperty() {
        System.setProperty(TomcatSystemProperty.HTTP_PROTOCOL_SYSPROPERTY, 'org.apache.coyote.http11.Http11NioProtocol')
        assert TomcatSystemProperty.getHttpProtocolHandlerClassName() == 'org.apache.coyote.http11.Http11NioProtocol'
    }

    @Test
    void testGetHttpsProtocolHandlerClassNameForNonExistingProperty() {
        assert TomcatSystemProperty.getHttpsProtocolHandlerClassName() == null
    }

    @Test
    void testGetHttpsProtocolHandlerClassNameForValidExistingProperty() {
        System.setProperty(TomcatSystemProperty.HTTPS_PROTOCOL_SYSPROPERTY, 'org.apache.coyote.http11.Http11NioProtocol')
        assert TomcatSystemProperty.getHttpsProtocolHandlerClassName() == 'org.apache.coyote.http11.Http11NioProtocol'
    }
    
    @Test
    void testGetAjpProtocolHandlerClassNameForNonExistingProperty() {
      assert TomcatSystemProperty.getAjpProtocolHandlerClassName() == null
    }
    
    @Test
    void testGetAjpProtocolHandlerClassNameForValidExistingProperty() {
      System.setProperty(TomcatSystemProperty.AJP_PROTOCOL_SYSPROPERTY, 'org.apache.coyote.ajp.AjpAprProtocol')
      assert TomcatSystemProperty.getAjpProtocolHandlerClassName() == 'org.apache.coyote.ajp.AjpAprProtocol'
    }
    
    @Test
    void testGetAjpPortForNonExistingProperty() {
        assert TomcatSystemProperty.getAjpPort() == null
    }

    @Test
    void testGetAjpPortForValidExistingProperty() {
        System.setProperty(TomcatSystemProperty.AJP_PORT_SYSPROPERTY, "8109")
        assert TomcatSystemProperty.getAjpPort() == 8109
    }
    
    @Test(expected = InvalidUserDataException.class)
    void testGetAjpPortForInvalidExistingProperty() {
        System.setProperty(TomcatSystemProperty.AJP_PORT_SYSPROPERTY, "xxx")
        TomcatSystemProperty.getAjpPort()
    }
}
