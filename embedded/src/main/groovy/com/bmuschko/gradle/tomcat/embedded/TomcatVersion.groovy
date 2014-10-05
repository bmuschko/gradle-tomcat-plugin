/*
 * Copyright 2012 the original author or authors.
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
package com.bmuschko.gradle.tomcat.embedded

/**
 * Tomcat server version.
 *
 * @author Benjamin Muschko
 */
enum TomcatVersion {
    VERSION_6X('6.0', 'Tomcat 6x', 'com.bmuschko.gradle.tomcat.embedded.Tomcat6xServer'),
    VERSION_7X('7.0', 'Tomcat 7x', 'com.bmuschko.gradle.tomcat.embedded.Tomcat7xServer'),
    VERSION_8X('8.0', 'Tomcat 8x', 'com.bmuschko.gradle.tomcat.embedded.Tomcat8xServer')

    private static final TOMCAT_VERSIONS = [:]

    static {
        values().each {
            TOMCAT_VERSIONS[it.specVersion] = it
        }

        TOMCAT_VERSIONS.asImmutable()
    }

    private final String specVersion
    private final String description
    private final String serverImplClass

    TomcatVersion(String specVersion, String description, String serverImplClass) {
        this.specVersion = specVersion
        this.description = description
        this.serverImplClass = serverImplClass
    }

    String getDescription() {
        description
    }

    String getServerImplClass() {
        serverImplClass
    }

    static TomcatVersion getTomcatVersionForSpec(String specVersion) {
        if(!TOMCAT_VERSIONS.containsKey(specVersion)) {
            throw new IllegalArgumentException("Unsupported specification version '$specVersion'")
        }

        TOMCAT_VERSIONS[specVersion]
    }

    static TomcatVersion getTomcatVersionForString(String version) {
        if(version.startsWith('6')) {
            return VERSION_6X
        }
        else if(version.startsWith('7')) {
            return VERSION_7X
        }
        else if(version.startsWith('8')) {
            return VERSION_8X
        }

        throw new IllegalArgumentException("Unsupported Tomcat version $version")
    }

    @Override
    String toString() {
        description
    }
}