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

import org.apache.tools.ant.AntClassLoader
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.UncheckedIOException
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.tomcat.embedded.TomcatServerFactory
import org.gradle.api.plugins.tomcat.internal.ShutdownMonitor
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.util.logging.Level

import static org.gradle.api.plugins.tomcat.internal.LoggingHandler.withJdkFileLogger

/**
 * Base class for all tasks which deploy a web application to an embedded Tomcat web container.
 *
 * @author Benjamin Muschko
 */
abstract class AbstractTomcatRunTask extends DefaultTask {
    static final CONFIG_FILE = 'META-INF/context.xml'
    boolean reloadable
    String contextPath
    Integer httpPort
    Integer httpsPort
    Integer stopPort
    String stopKey
    String httpProtocol
    String httpsProtocol
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
    @InputFile @Optional File keystoreFile
    String keystorePass
    @InputFile @Optional File truststoreFile
    String truststorePass
    String clientAuth
    File outputFile

    abstract void setWebApplicationContext()

    @TaskAction
    protected void start() {
        logger.info "Configuring Tomcat for ${getProject()}"

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
     * Creates Tomcat ClassLoader which consists of the Gradle runtime, Tomcat server and plugin classpath. The ClassLoader
     * is using a parent last strategy to make sure that the provided Gradle libraries get loaded only if they can't be
     * found in the application classpath.
     *
     * @return Tomcat ClassLoader
     */
    private URLClassLoader createTomcatClassLoader() {
        ClassLoader rootClassLoader = new AntClassLoader(getClass().classLoader, false)
        URLClassLoader pluginClassloader = new URLClassLoader(toURLArray(getBuildscriptClasspath().files), rootClassLoader)
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

    private void validateConfigurationAndStartTomcat() {
        validateConfiguration()

        withJdkFileLogger(getOutputFile(), true, Level.INFO) {
            startTomcat()
        }
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
                logger.info "Default web.xml = ${getWebDefaultXml().canonicalPath}"
            }
        }

        // Check the location of context.xml if it was provided.
        if(getConfigFile()) {
            if(!getConfigFile().exists()) {
                throw new InvalidUserDataException("context.xml file does not exist at location ${getConfigFile().canonicalPath}")
            }
            else {
                setResolvedConfigFile(getConfigFile().toURI().toURL())
                logger.info "context.xml = ${getResolvedConfigFile().toString()}"
            }
        }

        // Check HTTP(S) protocol handler class names
        if(!getHttpProtocol() || getHttpProtocol() == '') {
            throw new InvalidUserDataException('The provided HTTP protocol handler classname may not be null or blank')
        }
        else {
            logger.info "HTTP protocol handler classname = ${getHttpProtocol()}"
        }

        if(!getHttpsProtocol() || getHttpsProtocol() == '') {
            throw new InvalidUserDataException('The provided HTTPS protocol handler classname may not be null or blank')
        }
        else {
            logger.info "HTTPS protocol handler classname = ${getHttpsProtocol()}"
        }

        if(getOutputFile()) {
            if(getOutputFile().path == '') {
                throw new InvalidUserDataException('The provided output file may not be blank')
            }
            else {
                logger.info "Output file = ${getOutputFile().canonicalPath}"
            }
        }

        if(getEnableSSL()) {
            validateStore(getKeystoreFile(), getKeystorePass(), StoreType.KEY)
            validateStore(getTruststoreFile(), getTruststorePass(), StoreType.TRUST)
            def validClientAuthPhrases = ["true", "false", "want"]
            if(clientAuth && (!validClientAuthPhrases.contains(clientAuth))) {
              throw new InvalidUserDataException("If specified, clientAuth must be one of: ${validClientAuthPhrases}")
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
            logger.debug 'Starting Tomcat Server ...'

            setServer(createServer())
            getServer().home = getTemporaryDir().absolutePath
            getServer().realm = realm

            configureWebApplication()

            getServer().configureContainer()
            getServer().configureHttpConnector(getHttpPort(), getURIEncoding(), getHttpProtocol())

            if(getEnableSSL()) {
                if(!getKeystoreFile()) {
                    SSLKeystore sslKeystore = initSSLKeystore()
                    createSSLCertificate(sslKeystore)
                    keystoreFile = sslKeystore.keystore
                    keystorePass = sslKeystore.keyPassword
                }

                getServer().configureHttpsConnector(getHttpsPort(), getURIEncoding(), getHttpsProtocol(), getKeystoreFile().canonicalPath, getKeystorePass(), getTruststoreFile().canonicalPath, getTruststorePass(), getClientAuth())
            }

            // Start server
            getServer().start()

            logger.quiet 'Started Tomcat Server'
            logger.quiet "The Server is running at http://localhost:${getHttpPort()}${getServer().context.path}"

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
                logger.info 'Tomcat server exiting.'
            }
        }
    }

    /**
     * Initializes SSL keystore parameters.
     *
     * @return SSL keystore parameters
     */
    private SSLKeystore initSSLKeystore() {
        logger.info 'Generating temporary SSL keystore'
        final File keystore = new File("$project.buildDir/tmp/ssl/keystore")
        final String keyPassword = 'gradleTomcat'
        new SSLKeystore(keystore: keystore, keyPassword: keyPassword)
    }

    /**
     * Creates SSL certifacte.
     *
     * @param sslKeystore SSL keystore parameters
     */
    private void createSSLCertificate(SSLKeystore sslKeystore) {
        logger.info 'Creating SSL certificate'

        final File keystoreFile = sslKeystore.keystore

        if(!sslKeystore.keystore.parentFile.exists() && !sslKeystore.keystore.parentFile.mkdirs()) {
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
        logger.info 'Created SSL certificate'
    }
    
    /**
     * Validates that the necessary parameters have been provided for the specified key/trust store.
     *
     * @param file The file representing the store
     * @param password The password to the store
     * @param storeType identifies whether the store is a KeyStore or TrustStore
     */
    private void validateStore(File file, String password, StoreType storeType) {
        if(file == null ^ password == null) {
            throw new InvalidUserDataException('If you want to provide a ${storeType.description} then password and file may not be null or blank')
        }
        else if(file && password) {
        if(!file.exists()) {
                throw new InvalidUserDataException("${storeType.description} file does not exist at location ${file.canonicalPath}")
            }
            else {
                logger.info "${storeType.description} file = ${file}"
            }
        }
    }

    String getFullContextPath() {
        if(getContextPath() == '/' || getContextPath() == '') {
            return ''
        }

        getContextPath().startsWith('/') ? getContextPath() : '/' + getContextPath()
    }

    def createServer() {
        TomcatServerFactory.instance.tomcatServer
    }

    Integer getHttpPort() {
        Integer httpPortSystemProperty = TomcatSystemProperty.httpPort
        httpPortSystemProperty ?: httpPort
    }

    Integer getHttpsPort() {
        Integer httpsPortSystemProperty = TomcatSystemProperty.httpsPort
        httpsPortSystemProperty ?: httpsPort
    }

    Integer getStopPort() {
        Integer stopPortSystemProperty = TomcatSystemProperty.stopPort
        stopPortSystemProperty ?: stopPort
    }

    String getStopKey() {
        String stopKeySystemProperty = TomcatSystemProperty.stopKey
        stopKeySystemProperty ?: stopKey
    }

    Boolean getEnableSSL() {
        Boolean enableSSLSystemProperty = TomcatSystemProperty.enableSSL
        enableSSLSystemProperty ?: enableSSL
    }
    
    String getHttpProtocol() {
        String httpProtocolHandlerClassNameSystemProperty = TomcatSystemProperty.httpProtocolHandlerClassName
        httpProtocolHandlerClassNameSystemProperty ?: httpProtocol
    }

    String getHttpsProtocol() {
        String httpsProtocolHandlerClassNameSystemProperty = TomcatSystemProperty.httpsProtocolHandlerClassName
        httpsProtocolHandlerClassNameSystemProperty ?: httpsProtocol
    }
}

enum StoreType {

  TRUST("TrustStore"),
  KEY("KeyStore")  
  
  StoreType(String description) {
    this.description = description
  }
  
  String description
}
