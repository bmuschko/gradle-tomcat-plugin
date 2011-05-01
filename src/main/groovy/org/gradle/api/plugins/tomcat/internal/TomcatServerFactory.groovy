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
package org.gradle.api.plugins.tomcat.internal

import org.gradle.api.GradleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
    static final Logger LOGGER = LoggerFactory.getLogger(TomcatServerFactory.class)

    def getTomcatServer() {
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        Class appClass = resolveTomcatServerImpl(classLoader)
        appClass.newInstance()
    }

    private Class resolveTomcatServerImpl(ClassLoader classLoader) {
        Class tomcatServerImpl

        try {
            // Try to find embedded Tomcat implementation class introduced in version 7
            classLoader.findClass('org.apache.catalina.startup.Tomcat')
            LOGGER.info 'Resolved Tomcat 7x server implementation in classpath'
            tomcatServerImpl = classLoader.loadClass('org.gradle.api.plugins.tomcat.internal.Tomcat7xServer')
        }
        catch(ClassNotFoundException e) {
            LOGGER.info 'Resolved Tomcat 6x server implementation in classpath'
            tomcatServerImpl = classLoader.loadClass('org.gradle.api.plugins.tomcat.internal.Tomcat6xServer')
        }

        if(!tomcatServerImpl) {
            throw new GradleException('Unable to find embedded Tomcat server implementation in classpath.')
        }

        tomcatServerImpl
    }
}
