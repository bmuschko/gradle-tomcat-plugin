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

/**
 * Defines Tomcat plugin system properties.
 *
 * @author Benjamin Muschko
 */
final class TomcatSystemProperty {
    static final String HTTP_PORT_SYSPROPERTY = 'tomcat.http.port'
    static final String HTTPS_PORT_SYSPROPERTY = 'tomcat.https.port'
    static final String STOP_PORT_SYSPROPERTY = 'tomcat.stop.port'
    static final String STOP_KEY_SYSPROPERTY = 'tomcat.stop.key'
    static final String ENABLE_SSL_SYSPROPERTY = 'tomcat.enable.ssl'

    private TomcatSystemProperty() {}

    static Integer getHttpPort() {
        String httpPortSystemProperty = System.getProperty(HTTP_PORT_SYSPROPERTY)

        if(httpPortSystemProperty) {
            try {
                return httpPortSystemProperty.toInteger()
            }
            catch(NumberFormatException e) {
                throw new InvalidUserDataException("Bad HTTP port provided as system property: ${httpPortSystemProperty}", e)
            }
        }

        null
    }

    static Integer getHttpsPort() {
        String httpsPortSystemProperty = System.getProperty(HTTPS_PORT_SYSPROPERTY)

        if(httpsPortSystemProperty) {
            try {
                return httpsPortSystemProperty.toInteger()
            }
            catch(NumberFormatException e) {
                throw new InvalidUserDataException("Bad HTTPS port provided as system property: ${httpsPortSystemProperty}", e)
            }
        }

        null
    }

    static Integer getStopPort() {
        String stopPortSystemProperty = System.getProperty(STOP_PORT_SYSPROPERTY)

        if(stopPortSystemProperty) {
            try {
                return stopPortSystemProperty.toInteger()
            }
            catch(NumberFormatException e) {
                throw new InvalidUserDataException("Bad stop port provided as system property: ${stopPortSystemProperty}", e)
            }
        }

        null
    }

    static String getStopKey() {
        String stopKeySystemProperty = System.getProperty(STOP_KEY_SYSPROPERTY)

        if(stopKeySystemProperty) {
            return stopKeySystemProperty
        }

        null
    }

    static Boolean getEnableSSL() {
        String enableSSLSystemProperty = System.getProperty(ENABLE_SSL_SYSPROPERTY)

        if(enableSSLSystemProperty) {
            return new Boolean(enableSSLSystemProperty)
        }

        null
    }
}
