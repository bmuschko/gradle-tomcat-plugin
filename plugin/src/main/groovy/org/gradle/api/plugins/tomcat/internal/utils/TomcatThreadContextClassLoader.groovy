/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.plugins.tomcat.internal.utils

import org.apache.tools.ant.AntClassLoader
import org.gradle.api.UncheckedIOException

class TomcatThreadContextClassLoader implements ThreadContextClassLoader {
    /**
     * {@inheritDoc}
     */
    @Override
    void withClasspath(Set<File> buildscriptClasspathFiles, Set<File> tomcatClasspathFiles, Closure closure) {
        ClassLoader originalClassLoader = getClass().classLoader

        try {
            Thread.currentThread().contextClassLoader = createClassLoader(buildscriptClasspathFiles, tomcatClasspathFiles)
            closure()
        }
        finally {
            Thread.currentThread().contextClassLoader = originalClassLoader
        }
    }

    /**
     * Creates Tomcat ClassLoader which consists of the Gradle runtime, Tomcat server and plugin classpath. The ClassLoader
     * is using a parent last strategy to make sure that the provided Gradle libraries get loaded only if they can't be
     * found in the application classpath.
     *
     * @param buildscriptClasspathFiles Buildscript classpath files
     * @param tomcatClasspathFiles Tomcat classpath files
     * @return Tomcat ClassLoader
     */
    private URLClassLoader createClassLoader(Set<File> buildscriptClasspathFiles, Set<File> tomcatClasspathFiles) {
        ClassLoader rootClassLoader = new AntClassLoader(getClass().classLoader, false)
        URLClassLoader pluginClassloader = new URLClassLoader(toURLArray(buildscriptClasspathFiles), rootClassLoader)
        new URLClassLoader(toURLArray(tomcatClasspathFiles), pluginClassloader)
    }

    /**
     * Creates URL array from a set of files.
     *
     * @param files Files
     * @return URL array
     */
    private URL[] toURLArray(Set<File> files) {
        List<URL> urls = new ArrayList<URL>(files.size())

        files.each { file ->
            try {
                urls << file.toURI().toURL()
            }
            catch(MalformedURLException e) {
                throw new UncheckedIOException(e)
            }
        }

        urls.toArray(new URL[urls.size()])
    }
}
