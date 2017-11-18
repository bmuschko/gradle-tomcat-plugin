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
package com.bmuschko.gradle.tomcat.tasks

import com.bmuschko.gradle.tomcat.embedded.TomcatUser
import com.bmuschko.gradle.tomcat.embedded.factory.TomcatServerFactory
import com.bmuschko.gradle.tomcat.internal.ShutdownMonitor
import com.bmuschko.gradle.tomcat.internal.ssl.SSLKeyStore
import com.bmuschko.gradle.tomcat.internal.ssl.SSLKeyStoreImpl
import com.bmuschko.gradle.tomcat.internal.ssl.StoreType
import com.bmuschko.gradle.tomcat.internal.utils.ThreadContextClassLoader
import com.bmuschko.gradle.tomcat.internal.utils.TomcatThreadContextClassLoader
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*

import java.util.concurrent.CountDownLatch
import java.util.logging.Level

import static com.bmuschko.gradle.tomcat.internal.LoggingHandler.withJdkFileLogger

/**
 * Base class for all tasks which deploy a web application to an embedded Tomcat web container.
 *
 * @author Benjamin Muschko
 */
abstract class AbstractTomcatRun extends TomcatRunAlways {
    static final CONFIG_FILE = 'META-INF/context.xml'

    /**
     * Forces context scanning if you don't use a context file. Defaults to true.
     */
    @Input
    Boolean reloadable = Boolean.TRUE

    /**
     * The URL context path under which the web application will be registered. Defaults to WAR name.
     */
    @Input
    @Optional
    String contextPath

    /**
     * The TCP port which Tomcat should listen for HTTP requests. Defaults to 8080.
     */
    @Input
    Integer httpPort = 8080

    /**
     * The TCP port which Tomcat should listen for HTTPS requests. Defaults to 8443.
     */
    @Input
    @Optional
    Integer httpsPort = 8443

    /**
     * The TCP port which Tomcat should listen for admin requests. Defaults to 8081.
     */
    @Input
    Integer stopPort = 8081

    /**
     * The key to pass to Tomcat when requesting it to stop. Defaults to "stopKey".
     */
    @Input
    String stopKey = 'stopKey'

    /**
     * The HTTP protocol handler class name to be used. Defaults to {@link org.apache.coyote.http11.Http11Protocol}.
     */
    @Input
    String httpProtocol = 'org.apache.coyote.http11.Http11Protocol'

    /**
     * The HTTPS protocol handler class name to be used. Defaults to {@link org.apache.coyote.http11.Http11Protocol}.
     */
    @Input
    String httpsProtocol = 'org.apache.coyote.http11.Http11Protocol'

    /**
     * The default web.xml. If it doesn't get defined an instance of org.apache.catalina.servlets.DefaultServlet and
     * {@link org.apache.jasper.servlet.JspServlet} will be set up.
     */
    @InputFile
    @Optional
    File webDefaultXml

    /**
     * Defines additional runtime JARs or directories that are not provided by the web application.
     */
    @InputFiles
    Iterable<File> additionalRuntimeResources = []

    /**
     * Specifies the character encoding used to decode the URI bytes by the HTTP Connector. Defaults to "UTF-8".
     */
    @Input
    String URIEncoding = 'UTF-8'

    /**
     * Specifies whether the Tomcat server should run in the background. When true, this task completes as soon as the
     * server has started. When false, this task blocks until the Tomcat server is stopped. Defaults to false.
     */
    @Input
    Boolean daemon = Boolean.FALSE

    /**
     * Classpath for Tomcat libraries.
     */
    @InputFiles
    FileCollection tomcatClasspath

    /**
     * The path to the Tomcat context XML file.
     */
    @InputFile
    @Optional
    File configFile

    /**
     * Determines whether the HTTPS connector should be created. Defaults to false.
     */
    @Input
    Boolean enableSSL = Boolean.FALSE

    /**
     * Doesn't override existing SSL key. Defaults to false.
     */
    @Input
    Boolean preserveSSLKey = Boolean.FALSE

    /**
     * The keystore file to use for SSL, if enabled (by default, a keystore will be generated).
     */
    @InputFile
    @Optional
    File keystoreFile

    /**
     * The keystore password to use for SSL, if enabled.
     */
    @Input
    @Optional
    String keystorePass

    /**
     * The truststore file to use for SSL, if enabled.
     */
    @InputFile
    @Optional
    File truststoreFile

    /**
     * The truststore password to use for SSL, if enabled.
     */
    @Input
    @Optional
    String truststorePass

    /**
     * The clientAuth setting to use, values may be: "true", "false" or "want".  Defaults to "false".
     */
    @Input
    @Optional
    String clientAuth = 'false'

    /**
     * The file to write Tomcat log messages to. If the file already exists new messages will be appended.
     */
    @OutputFile
    @Optional
    File outputFile

    /**
     * The TCP port which Tomcat should listen for AJP requests. Defaults to 8009.
     */
    @Input
    Integer ajpPort = 8009

    /**
     * The AJP protocol handler class name to be used. Defaults to {@link org.apache.coyote.ajp.AjpProtocol}.
     */
    @Input
    String ajpProtocol = 'org.apache.coyote.ajp.AjpProtocol'

    /**
     * The list of Tomcat users. Defaults to an empty list.
     */
    @Input
    @Optional
    List<TomcatUser> users = []

    @Internal
    def server

    @Internal
    def realm

    @Internal
    URL resolvedConfigFile

    private Thread shutdownHook

    private final ThreadContextClassLoader threadContextClassLoader = new TomcatThreadContextClassLoader()
    private final SSLKeyStore sslKeyStore = new SSLKeyStoreImpl()

    abstract void setWebApplicationContext()

    @TaskAction
    protected void start() {
        logger.info "Configuring Tomcat for ${getProject()}"

        threadContextClassLoader.withClasspath(getTomcatClasspath().files) {
            validateConfigurationAndStartTomcat()
        }
    }

    void validateConfigurationAndStartTomcat() {
        validateConfiguration()

        withJdkFileLogger(getOutputFile(), true, Level.INFO) {
            startTomcat()
        }
    }

    /**
     * Validates configuration and throws an exception if
     */
    protected void validateConfiguration() {
        // Check existence of default web.xml if provided
        if(getWebDefaultXml()) {
            logger.info "Default web.xml = ${getWebDefaultXml().canonicalPath}"
        }

        // Check the location of context.xml if it was provided.
        if(getConfigFile()) {
            setResolvedConfigFile(getConfigFile().toURI().toURL())
            logger.info "context.xml = ${getResolvedConfigFile().toString()}"
        }

        // Check HTTP(S) protocol handler class names
        if(getHttpProtocol()) {
            logger.info "HTTP protocol handler classname = ${getHttpProtocol()}"
        }

        if(getHttpsProtocol()) {
            logger.info "HTTPS protocol handler classname = ${getHttpsProtocol()}"
        }

        if(getOutputFile()) {
            logger.info "Output file = ${getOutputFile().canonicalPath}"
        }

        if(getEnableSSL()) {
            validateStore(getKeystoreFile(), getKeystorePass(), StoreType.KEY)
            validateStore(getTruststoreFile(), getTruststorePass(), StoreType.TRUST)
            def validClientAuthPhrases = ["true", "false", "want"]

            if(getClientAuth() && (!validClientAuthPhrases.contains(getClientAuth()))) {
                throw new InvalidUserDataException("If specified, clientAuth must be one of: ${validClientAuthPhrases}")
            }
        }
    }

    /**
     * Validates that the necessary parameters have been provided for the specified key/trust store.
     *
     * @param storeFile The file representing the store
     * @param keyStorePassword The password to the store
     * @param storeType identifies whether the store is a KeyStore or TrustStore
     */
    private void validateStore(File storeFile, String keyStorePassword, StoreType storeType) {
        if(!storeFile ^ !keyStorePassword) {
            throw new InvalidUserDataException("If you want to provide a $storeType.description then password and file may not be null or blank")
        }
        else if(storeFile && keyStorePassword) {
            if(!storeFile.exists()) {
                throw new InvalidUserDataException("$storeType.description file does not exist at location $storeFile.canonicalPath")
            }
            else {
                logger.info "$storeType.description file = ${storeFile}"
            }
        }
    }

    protected void addWebappResource(File resource) {
        if(resource.exists()) {
            server.addWebappResource(resource)
        }
    }

    /**
     * Configures web application
     */
    protected void configureWebApplication() {
        setWebApplicationContext()
        server.createLoader(Thread.currentThread().contextClassLoader)

        logger.info "Additional runtime resources classpath = ${getAdditionalRuntimeResources()}"

        getAdditionalRuntimeResources().each { file ->
            addWebappResource(file)
        }

        server.context.reloadable = getReloadable()
        server.configureDefaultWebXml(getWebDefaultXml())

        if(getResolvedConfigFile()) {
            server.configFile = getResolvedConfigFile()
        }
    }

    void startTomcat() {
        try {
            logger.debug 'Starting Tomcat Server ...'

            server = TomcatServerFactory.instance.tomcatServer
            server.home = getTemporaryDir().absolutePath
            server.realm = realm

            configureWebApplication()

            server.configureContainer()
            server.configureHttpConnector(getHttpPort(), getURIEncoding(), getHttpProtocol())
            server.configureAjpConnector(getAjpPort(), getURIEncoding(), getAjpProtocol())

            getUsers().each { TomcatUser user ->
                server.configureUser(user)
            }

            if(getEnableSSL()) {
                if(!getKeystoreFile()) {
                    final File keystore = project.file("$project.buildDir/tmp/ssl/keystore")
                    final String keyPassword = 'gradleTomcat'
                    sslKeyStore.createSSLCertificate(keystore, keyPassword, getPreserveSSLKey())
                    keystoreFile = keystore
                    keystorePass = keyPassword
                }

                if(getTruststoreFile()) {
                    server.configureHttpsConnector(getHttpsPort(), getURIEncoding(), getHttpsProtocol(), getKeystoreFile(),
                                                        getKeystorePass(), getTruststoreFile(), getTruststorePass(), getClientAuth())
                }
                else {
                    server.configureHttpsConnector(getHttpsPort(), getURIEncoding(), getHttpsProtocol(), getKeystoreFile(), getKeystorePass())
                }
            }

            final CountDownLatch startupBarrier = new CountDownLatch(1)
            server.addStartUpLifecycleListener(startupBarrier, getDaemon())

            // Start server
            server.start()

            addShutdownHook()

            Thread shutdownMonitor = new ShutdownMonitor(getStopPort(), getStopKey(), server, getDaemon())
            shutdownMonitor.start()

            startupBarrier.await()
        }
        catch(Exception e) {
            stopServer()
            throw new GradleException('An error occurred starting the Tomcat server.', e)
        }
        finally {
            removeShutdownHook()
        }
    }

    @Internal
    protected String getFullContextPath() {
        if(getContextPath() == '/' || getContextPath() == '') {
            return ''
        }

        getContextPath().startsWith('/') ? getContextPath() : '/' + getContextPath()
    }

    private void addShutdownHook() {
        shutdownHook = new Thread(new Runnable() {
            @Override
            void run() {
                stopServer()
            }
        })

        Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    private void removeShutdownHook() {
        if(shutdownHook) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook)
        }
    }

    private void stopServer() {
        if(server && !server.stopped) {
            server.stop()
        }
    }

    /**
     * Adds a Tomcat user by providing required fields. Allows for adding users in enhanced tasks without having to
     * know the plugin's API class representing a Tomcat user.
     *
     * @param username Username
     * @param password Password
     * @param roles Roles
     */
    void user(String username, String password, List<String> roles) {
        users << new TomcatUser(username: username, password: password, roles: roles)
    }
}
