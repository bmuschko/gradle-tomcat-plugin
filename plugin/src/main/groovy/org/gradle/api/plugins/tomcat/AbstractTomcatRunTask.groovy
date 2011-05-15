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

import org.apache.catalina.Realm
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.UncheckedIOException
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ClassPathRegistry
import org.gradle.api.internal.ConventionTask
import org.gradle.api.internal.DefaultClassPathRegistry
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.tomcat.embedded.TomcatServerFactory
import org.gradle.api.plugins.tomcat.internal.ShutdownMonitor
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * Base class for all tasks which deploy a web application to an embedded Tomcat web container.
 *
 * @author Benjamin Muschko
 */
abstract class AbstractTomcatRunTask extends ConventionTask {
    static final Logger LOGGER = Logging.getLogger(AbstractTomcatRunTask.class)
    static final CONFIG_FILE = 'META-INF/context.xml'
    boolean reloadable
    String contextPath
    Integer httpPort
    Integer stopPort
    String stopKey
    File webDefaultXml
    def server
    Realm realm
    Iterable<File> additionalRuntimeJars = new ArrayList<File>()
    String URIEncoding
    boolean daemon
    FileCollection serverClasspath
    File configFile
    URL resolvedConfigFile

    abstract void setWebApplicationContext()

    @TaskAction
    protected void start() {
        LOGGER.info "Configuring Tomcat for ${getProject()}"

        ClassLoader originalClassLoader = getClass().classLoader
        URLClassLoader tomcatClassloader = createTomcatClassLoader()

        try {
            Thread.currentThread().setContextClassLoader(tomcatClassloader)
            validateConfigurationAndStartTomcat()
        }
        finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader)
        }
    }

    /**
     * Creates Tomcat classloader which consists of the Gradle runtime and Tomcat server classpath.
     *
     * @return Tomcat classloader
     */
    private URLClassLoader createTomcatClassLoader() {
        ClassPathRegistry classPathRegistry = new DefaultClassPathRegistry()
        URL[] runtimeClasspath = classPathRegistry.getClassPathUrls('GRADLE_RUNTIME')
        ClassLoader rootClassLoader = ClassLoader.systemClassLoader.parent
        URLClassLoader gradleClassloader = new URLClassLoader(runtimeClasspath, rootClassLoader)
        new URLClassLoader(toURLArray(getServerClasspath().files), gradleClassloader)
    }

    URL[] toURLArray(Collection<File> files) {
        List<URL> urls = new ArrayList<URL>(files.size())

        for(File file : files) {
            try {
                urls.add(file.toURI().toURL())
            }
            catch(MalformedURLException e) {
                throw new UncheckedIOException(e)
            }
        }

        urls.toArray(new URL[urls.size()]);
    }

    private void validateConfigurationAndStartTomcat() {
        validateConfiguration()
        startTomcat()
    }

    /**
     * Validates configuration and throws an exception if
     */
    void validateConfiguration() {
        // Check existence of default web.xml if provided
        if(getWebDefaultXml()) {
            if(!getWebDefaultXml().exists()) {
                throw new InvalidUserDataException('The provided default web.xml file does not exist')
            }
            else {
                LOGGER.info "Default web.xml = ${getWebDefaultXml().canonicalPath}"
            }
        }

        // Check the location of context.xml if it was provided.
        if(getConfigFile()) {
            if(!getConfigFile().exists()) {
                throw new InvalidUserDataException("context.xml file does not exist at location ${getConfigFile().canonicalPath}")
            }
            else {
                setResolvedConfigFile(getConfigFile().toURI().toURL())
                LOGGER.info "context.xml = ${getResolvedConfigFile().toString()}"
            }
        }
    }

    /**
     * Configures web application
     */
    void configureWebApplication() {
        setWebApplicationContext()
        getServer().createLoader(Thread.currentThread().getContextClassLoader())

        getAdditionalRuntimeJars().each { additionalRuntimeJar ->
            getServer().getContext().getLoader().addRepository(additionalRuntimeJar.toURI().toURL().toString())
        }

        getServer().getContext().setReloadable(reloadable)
        getServer().configureDefaultWebXml(getWebDefaultXml())

        if(getResolvedConfigFile()) {
            getServer().setConfigFile(getResolvedConfigFile())
        }
    }

    void startTomcat() {
        try {
            LOGGER.debug 'Starting Tomcat Server ...'

            setServer(createServer())
            getServer().setHome(getTemporaryDir().getAbsolutePath())
            getServer().setRealm(realm)

            configureWebApplication()

            getServer().configureContainer(getHttpPort(), getURIEncoding())

            // Start server
            getServer().start()

            LOGGER.lifecycle 'Started Tomcat Server'
            LOGGER.lifecycle "The Server is running at http://localhost:${getHttpPort()}${getServer().getContext().path}"

            Thread shutdownMonitor = new ShutdownMonitor(getStopPort(), getStopKey(), getServer(), daemon)
            shutdownMonitor.start()

            if(!daemon) {
                shutdownMonitor.join()
            }
        }
        catch(Exception e) {
            throw new GradleException('An error occurred starting the Tomcat server.', e)
        }
        finally {
            if(!daemon) {
                LOGGER.info 'Tomcat server exiting.'
            }
        }
    }

    String getFullContextPath() {
        getContextPath().startsWith("/") ? getContextPath() : "/" + getContextPath()
    }

    def createServer() {
        TomcatServerFactory.instance.tomcatServer
    }

    Integer getHttpPort() {
        Integer httpPortSystemProperty = TomcatSystemProperty.httpPort
        httpPortSystemProperty ? httpPortSystemProperty : httpPort
    }

    Integer getStopPort() {
        Integer stopPortSystemProperty = TomcatSystemProperty.stopPort
        stopPortSystemProperty ? stopPortSystemProperty : stopPort
    }

    String getStopKey() {
        String stopKeySystemProperty = TomcatSystemProperty.stopKey
        stopKeySystemProperty ? stopKeySystemProperty : stopKey
    }

    @InputFile
    @Optional
    File getWebDefaultXml() {
        webDefaultXml
    }

    @InputFiles
    Iterable<File> getAdditionalRuntimeJars() {
        additionalRuntimeJars
    }

    @Optional
    String getURIEncoding() {
        URIEncoding
    }

    @InputFile
    @Optional
    File getConfigFile() {
        configFile
    }
}
