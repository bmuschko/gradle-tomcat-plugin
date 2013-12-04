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
package org.gradle.api.plugins.tomcat.embedded

import org.gradle.api.GradleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.jar.Manifest

/**
 * Tomcat server factory which resolves its implementation by checking the classpath for a specific
 * implementation class. Acts as a bootstrapper to make sure server implementation is running in
 * classloader different from Gradle's runtime classloader. This will make sure that there are no conflicting
 * libraries (e.g. JSP API provided by Gradle's Jetty plugin out-of-the-box).
 *
 * @author Benjamin Muschko
 */
@Singleton
class TomcatServerFactory {
    static final Logger LOGGER = LoggerFactory.getLogger(TomcatServerFactory)

    def getTomcatServer() {
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        Class appClass = resolveTomcatServerImpl(classLoader)
        appClass.newInstance()
    }

    private Class resolveTomcatServerImpl(ClassLoader classLoader) {
        String txtVersion
        classLoader.getResources('META-INF/MANIFEST.MF').every {
            InputStream inputStream = it.openStream()
            try {
                Manifest manifest = new Manifest(inputStream)
                def attrs = manifest.mainAttributes
                if(attrs.getValue('Specification-Title') == 'Apache Tomcat') {
                    txtVersion = attrs.getValue('Specification-Version')
                }
            }
            finally {
                try {
                    inputStream.close()
                }
                catch(any) {}
            }

            !txtVersion
        }

        Class tomcatServerImpl
        if(txtVersion == '8.0') {
            tomcatServerImpl = classLoader.loadClass('org.gradle.api.plugins.tomcat.embedded.Tomcat8xServer')
            LOGGER.info 'Resolved Tomcat 8x server implementation in classpath'
        } else if(txtVersion == '7.0') {
            tomcatServerImpl = classLoader.loadClass('org.gradle.api.plugins.tomcat.embedded.Tomcat7xServer')
            LOGGER.info 'Resolved Tomcat 7x server implementation in classpath'
        } else if(txtVersion == '6.0') {
            tomcatServerImpl = classLoader.loadClass('org.gradle.api.plugins.tomcat.embedded.Tomcat6xServer')
            LOGGER.info 'Resolved Tomcat 6x server implementation in classpath'
        } else {
            throw new GradleException('Unable to find embedded Tomcat server implementation in classpath.')
        }

        tomcatServerImpl
    }
}
