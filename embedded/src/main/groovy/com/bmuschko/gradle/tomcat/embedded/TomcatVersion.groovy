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
 */
enum TomcatVersion {

    // @see https://wiki.apache.org/tomcat/TomcatVersions

    VERSION_6_0_X('6.0' , 'Tomcat 6.0.x'),
    VERSION_7_0_X('7.0' , 'Tomcat 7.0.x'),
    VERSION_8_0_X('8.0' , 'Tomcat 8.0.x'),
    VERSION_8_5_X('8.5',  'Tomcat 8.5.x'),
    VERSION_9_0_X('9.0',  'Tomcat 9.0.x')

    private static final TOMCAT_VERSIONS = [:]

    static {
        values().each {
            TOMCAT_VERSIONS[it.specVersion] = it
        }

        TOMCAT_VERSIONS.asImmutable()
    }

    private final String specVersion
    private final String description

    TomcatVersion(String specVersion, String description) {
        this.specVersion = specVersion
        this.description = description
    }

    String getDescription() {
        description
    }

    // example from catalina.jar - META-INF/MANIFEST.MF:
    //   Specification-Version: 8.5
    static TomcatVersion getTomcatVersionForSpec(String specVersion) {
        if(!TOMCAT_VERSIONS.containsKey(specVersion)) {
            throw new IllegalArgumentException("Unsupported specification version '$specVersion'")
        }

        TOMCAT_VERSIONS[specVersion]
    }

    static TomcatVersion getTomcatVersionForString(String version) {
        if(version.startsWith('6')) {
            return VERSION_6_0_X
        }
        else if(version.startsWith('7')) {
            return VERSION_7_0_X
        }
        else if(version.startsWith('8.0')) {
            return VERSION_8_0_X
        }
        else if(version.startsWith('8.5')) {
            return VERSION_8_5_X
        }
        else if(version.startsWith('9')) {
            return VERSION_9_0_X
        }

        throw new IllegalArgumentException("Unsupported Tomcat version $version")
    }

    @Override
    String toString() {
        description
    }
}