/*
 * Copyright 2011 the original author or authors.
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
package com.bmuschko.gradle.tomcat.embedded.factory

import com.bmuschko.gradle.tomcat.embedded.*
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException

/**
 * Tomcat server factory which resolves its implementation by checking the classpath for a specific
 * implementation class. Acts as a bootstrapper to make sure server implementation is running in
 * classloader different from Gradle's runtime classloader. This will make sure that there are no conflicting
 * libraries (e.g. JSP API provided by Gradle's Jetty plugin out-of-the-box).
 */
@Singleton
@Slf4j
class TomcatServerFactory {
    ManifestReader manifestReader = new TomcatSpecVersionManifestReader()

    def getTomcatServer() {
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        resolveTomcatServerImpl(classLoader)
    }

    private TomcatServer resolveTomcatServerImpl(ClassLoader classLoader) {
        String specVersion = readTomcatSpecVersionFromManifest(classLoader)
        TomcatVersion tomcatVersion = resolveTomcatVersion(specVersion)
        log.info "Resolved $tomcatVersion.description server implementation in classpath"

        switch(tomcatVersion) {
            case TomcatVersion.VERSION_6_0_X: return new Tomcat6xServer()
            case TomcatVersion.VERSION_7_0_X: return new Tomcat7xServer()
            case TomcatVersion.VERSION_8_0_X: return new Tomcat8xServer()
            case TomcatVersion.VERSION_8_5_X: return new Tomcat85xServer()
            case TomcatVersion.VERSION_9_0_X: return new Tomcat9xServer()
        }
    }

    /**
     * Reads Tomcat specification version from manifest files.
     *
     * @param classLoader Classloader
     * @return Specification version
     */
    private String readTomcatSpecVersionFromManifest(ClassLoader classLoader) {
        String specVersion

        classLoader.getResources('META-INF/MANIFEST.MF').every {
            specVersion = manifestReader.readAttributeValue(it)
            !specVersion
        }

        if(!specVersion) {
            throw new GradleException('Unable to resolve Tomcat server specification version.')
        }

        specVersion
    }

    /**
     * Gets Tomcat version for resolved spec.
     *
     * @param spec
     * @return Tomcat version
     */
    private TomcatVersion resolveTomcatVersion(String spec) {
        TomcatVersion tomcatVersion

        try {
            tomcatVersion = TomcatVersion.getTomcatVersionForSpec(spec)
        }
        catch(IllegalArgumentException e) {
            throw new GradleException('Unable to find embedded Tomcat server implementation in classpath.', e)
        }

        tomcatVersion
    }
}
