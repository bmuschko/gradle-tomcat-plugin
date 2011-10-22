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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.UncheckedIOException
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ClassPathRegistry
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
abstract class AbstractTomcatRunTask extends DefaultTask {
    static final Logger LOGGER = Logging.getLogger(AbstractTomcatRunTask.class)
    static final CONFIG_FILE = 'META-INF/context.xml'
    boolean reloadable
    String contextPath
    Integer httpPort
    Integer httpsPort
    Integer stopPort
    String stopKey
    @InputFile @Optional File webDefaultXml
    def server
    def realm
    @InputFiles Iterable<File> additionalRuntimeJars = []
    @Optional String URIEncoding
    boolean daemon
    FileCollection buildscriptClasspath
    FileCollection tomcatClasspath
    @InputFile @Optional File configFile
    URL resolvedConfigFile
    Boolean enableSSL

    abstract void setWebApplicationContext()

    @TaskAction
    protected void start() {
        LOGGER.info "Configuring Tomcat for ${getProject()}"

        ClassLoader originalClassLoader = getClass().classLoader
        URLClassLoader tomcatClassloader = createTomcatClassLoader()

        try {
            Thread.currentThread().contextClassLoader = tomcatClassloader
            validateConfigurationAndStartTomcat()
        }
        finally {
            Thread.currentThread().contextClassLoader = originalClassLoader
        }
    }

    /**
     * Creates Tomcat classloader which consists of the Groovy runtime, Tomcat serverand plugin classpath.
     *
     * @return Tomcat classloader
     */
    private URLClassLoader createTomcatClassLoader() {
        ClassPathRegistry classPathRegistry = new DefaultClassPathRegistry()
        URL[] runtimeClasspath = classPathRegistry.getClassPathUrls('GRADLE_RUNTIME')
        ClassLoader rootClassLoader = ClassLoader.systemClassLoader.parent
        URLClassLoader groovyClassloader = new URLClassLoader(filterGroovyAllLibrary(runtimeClasspath), rootClassLoader)
        URLClassLoader pluginClassloader = new URLClassLoader(toURLArray(getBuildscriptClasspath().files), groovyClassloader)
        new URLClassLoader(toURLArray(getTomcatClasspath().files), pluginClassloader)
    }

    private URL[] toURLArray(Collection<File> files) {
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

    private URL[] filterGroovyAllLibrary(URL[] runtimeClasspath) {
        for(URL url : runtimeClasspath) {
            String filenameWithoutPath = url.file.substring(url.file.lastIndexOf('/') + 1, url.file.length())
            if(filenameWithoutPath.startsWith('groovy-all-')) {
                return [url] as URL[]
            }
        }

        throw new GradleException('Groovy libraries could not be found in the Gradle runtime classpath!')
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
        getServer().createLoader(Thread.currentThread().contextClassLoader)

        getAdditionalRuntimeJars().each { additionalRuntimeJar ->
            getServer().context.loader.addRepository(additionalRuntimeJar.toURI().toURL().toString())
        }

        getServer().context.reloadable = reloadable
        getServer().configureDefaultWebXml(getWebDefaultXml())

        if(getResolvedConfigFile()) {
            getServer().configFile = getResolvedConfigFile()
        }
    }

    void startTomcat() {
        try {
            LOGGER.debug 'Starting Tomcat Server ...'

            setServer(createServer())
            getServer().home = getTemporaryDir().absolutePath
            getServer().realm = realm

            configureWebApplication()

            getServer().configureContainer()
            getServer().configureHttpConnector(getHttpPort(), getURIEncoding())

            if(getEnableSSL()) {
                SSLKeystore sslKeystore = initSSLKeystore()
                createSSLCertificate(sslKeystore)
                getServer().configureHttpsConnector(getHttpsPort(), getURIEncoding(), sslKeystore.keystore, sslKeystore.keyPassword)
            }

            // Start server
            getServer().start()

            LOGGER.lifecycle 'Started Tomcat Server'
            LOGGER.lifecycle "The Server is running at http://localhost:${getHttpPort()}${getServer().context.path}"

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

    /**
     * Initializes SSL keystore parameters.
     *
     * @return SSL keystore parameters
     */
    private SSLKeystore initSSLKeystore() {
        final String keystore = "$project.buildDir/tmp/ssl/keystore"
        final String keyPassword = 'gradleTomcat'
        new SSLKeystore(keystore: keystore, keyPassword: keyPassword)
    }

    /**
     * Creates SSL certifacte.
     *
     * @param sslKeystore SSL keystore parameters
     */
    private void createSSLCertificate(SSLKeystore sslKeystore) {
        LOGGER.info 'Creating SSL certificate'

        final File keystoreFile = new File(sslKeystore.keystore)

        if(!keystoreFile.parentFile.exists() && !keystoreFile.parentFile.mkdirs()) {
            throw new GradleException("Unable to create keystore folder: $keystoreFile.parentFile.canonicalPath")
        }

        if(keystoreFile.exists()) {
            keystoreFile.delete()
        }

        String[] keytoolArgs = ["-genkey", "-alias", "localhost", "-dname",
                "CN=localhost,OU=Test,O=Test,C=US", "-keyalg", "RSA",
                "-validity", "365", "-storepass", "key", "-keystore",
                sslKeystore.keystore, "-storepass", sslKeystore.keyPassword,
                "-keypass", sslKeystore.keyPassword]
        Class<?> keyToolClass

        try {
            keyToolClass = Class.forName('sun.security.tools.KeyTool')
        }
        catch(ClassNotFoundException e) {
            keyToolClass = Class.forName('com.ibm.crypto.tools.KeyTool')
        }

        keyToolClass.main(keytoolArgs)
        LOGGER.info 'Created SSL certificate'
    }

    String getFullContextPath() {
        getContextPath().startsWith('/') ? getContextPath() : '/' + getContextPath()
    }

    def createServer() {
        TomcatServerFactory.instance.tomcatServer
    }

    Integer getHttpPort() {
        Integer httpPortSystemProperty = TomcatSystemProperty.httpPort
        httpPortSystemProperty ? httpPortSystemProperty : httpPort
    }

    Integer getHttpsPort() {
        Integer httpsPortSystemProperty = TomcatSystemProperty.httpsPort
        httpsPortSystemProperty ? httpsPortSystemProperty : httpsPort
    }

    Integer getStopPort() {
        Integer stopPortSystemProperty = TomcatSystemProperty.stopPort
        stopPortSystemProperty ? stopPortSystemProperty : stopPort
    }

    String getStopKey() {
        String stopKeySystemProperty = TomcatSystemProperty.stopKey
        stopKeySystemProperty ? stopKeySystemProperty : stopKey
    }

    Boolean getEnableSSL() {
        Boolean enableSSLSystemProperty = TomcatSystemProperty.enableSSL
        enableSSLSystemProperty ? enableSSLSystemProperty : enableSSL
    }
}
