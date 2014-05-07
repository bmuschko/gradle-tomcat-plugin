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
package org.gradle.api.plugins.tomcat.tasks

import org.apache.tools.ant.AntClassLoader
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.UncheckedIOException
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.tomcat.SSLKeystore
import org.gradle.api.plugins.tomcat.embedded.TomcatServerFactory
import org.gradle.api.plugins.tomcat.embedded.TomcatVersion
import org.gradle.api.plugins.tomcat.internal.ShutdownMonitor
import org.gradle.api.plugins.tomcat.internal.StoreType
import org.gradle.api.tasks.*

import java.util.logging.Level

import static org.gradle.api.plugins.tomcat.internal.LoggingHandler.withJdkFileLogger

/**
 * Base class for all tasks which deploy a web application to an embedded Tomcat web container.
 *
 * @author Benjamin Muschko
 */
abstract class AbstractTomcatRun extends Tomcat {
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
     * The HTTP protocol handler class name to be used. Defaults to "org.apache.coyote.http11.Http11Protocol".
     */
    @Input
    String httpProtocol = 'org.apache.coyote.http11.Http11Protocol'

    /**
     * The HTTPS protocol handler class name to be used. Defaults to "org.apache.coyote.http11.Http11Protocol".
     */
    @Input
    String httpsProtocol = 'org.apache.coyote.http11.Http11Protocol'

    /**
     * The default web.xml. If it doesn't get defined an instance of org.apache.catalina.servlets.DefaultServlet and
     * org.apache.jasper.servlet.JspServlet will be set up.
     */
    @InputFile
    @Optional
    File webDefaultXml

    /**
     * Defines additional runtime JARs that are not provided by the web application.
     */
    @InputFiles
    Iterable<File> additionalRuntimeJars = []

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
     * The build script's classpath.
     */
    @InputFiles
    FileCollection buildscriptClasspath

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
     * The AJP protocol handler class name to be used. Defaults to "org.apache.coyote.ajp.AjpProtocol".
     */
    @Input
    String ajpProtocol = 'org.apache.coyote.ajp.AjpProtocol'

    def server
    def realm
    URL resolvedConfigFile

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
            if(getClientAuth() && (!validClientAuthPhrases.contains(getClientAuth()))) {
                throw new InvalidUserDataException("If specified, clientAuth must be one of: ${validClientAuthPhrases}")
            }
        }
    }


    /**
     * Checks if used Tomcat version is 8.x.
     *
     * @return Flag
     */
    protected boolean isTomcat8x() {
        getServer().version == TomcatVersion.VERSION_8X
    }

    /**
     * Checks if used Tomcat version is 7.x.
     *
     * @return Flag
     */
    protected boolean isTomcat7x() {
        getServer().version == TomcatVersion.VERSION_7X
    }

    /**
     * Checks if used Tomcat version is 6.x.
     *
     * @return Flag
     */
    protected boolean isTomcat6x() {
        getServer().version == TomcatVersion.VERSION_6X
    }

    protected def getResourceSetType(String name) {
        ClassLoader loader = Thread.currentThread().contextClassLoader
        Class resourceSetTypeClass = loader.loadClass('org.apache.catalina.WebResourceRoot$ResourceSetType')
        def type = resourceSetTypeClass.enumConstants.find { it.name() == name }

        type
    }

    protected void addWebappResource(File resource) {
        if(isTomcat8x()) {
            if (resource.exists()) {
                getServer().context.resources.createWebResourceSet(getResourceSetType('PRE'), '/WEB-INF/classes', resource.toURI().toURL(), '/')
            }
        }
        else {
            getServer().context.loader.addRepository(resource.toURI().toURL().toString())
        }
    }

    /**
     * Configures web application
     */
    void configureWebApplication() {
        setWebApplicationContext()
        getServer().createLoader(Thread.currentThread().contextClassLoader)

        getAdditionalRuntimeJars().each { additionalRuntimeJar ->
            addWebappResource(additionalRuntimeJar)
        }

        getServer().context.reloadable = getReloadable()
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
            getServer().configureAjpConnector(getAjpPort(), getURIEncoding(), getAjpProtocol())

            if(getEnableSSL()) {
                if(!getKeystoreFile()) {
                    SSLKeystore sslKeystore = initSSLKeystore()
                    createSSLCertificate(sslKeystore)
                    keystoreFile = sslKeystore.keystore
                    keystorePass = sslKeystore.keyPassword
                }

                if(getTruststoreFile()) {
                    getServer().configureHttpsConnector(getHttpsPort(), getURIEncoding(), getHttpsProtocol(), getKeystoreFile(),
                                                        getKeystorePass(), getTruststoreFile(), getTruststorePass(), getClientAuth())
                }
                else {
                    getServer().configureHttpsConnector(getHttpsPort(), getURIEncoding(), getHttpsProtocol(), getKeystoreFile(), getKeystorePass())
                }
            }

            // Start server
            getServer().start()

            logger.quiet 'Started Tomcat Server'
            logger.quiet "The Server is running at http://localhost:${getHttpPort()}${getServer().context.path}"

            Thread shutdownMonitor = new ShutdownMonitor(getStopPort(), getStopKey(), getServer(), daemon)
            shutdownMonitor.start()

            if(!getDaemon()) {
                shutdownMonitor.join()
            }
        }
        catch(Exception e) {
            throw new GradleException('An error occurred starting the Tomcat server.', e)
        }
        finally {
            if(!getDaemon()) {
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
     * Creates SSL certificate.
     *
     * @param sslKeystore SSL keystore parameters
     */
    private void createSSLCertificate(SSLKeystore sslKeystore) {
        logger.info 'Creating SSL certificate'

        prepareKeystoreDirectory(sslKeystore)

        if(sslKeystore.keystore.exists()) {
            if(getPreserveSSLKey()) {
                return
            }

            sslKeystore.keystore.delete()
        }

        invokeKeyTool(sslKeystore)

        logger.info 'Created SSL certificate'
    }

    /**
     * Prepares keystore directory.
     *
     * @param sslKeystore SSL keystore
     */
    private void prepareKeystoreDirectory(SSLKeystore sslKeystore) {
        final File keystoreFile = sslKeystore.keystore

        if(!keystoreFile.parentFile.exists() && !keystoreFile.parentFile.mkdirs()) {
            throw new GradleException("Unable to create keystore directory: $keystoreFile.parentFile.canonicalPath")
        }
    }

    /**
     * Invokes keytool to create SSL certificate.
     *
     * @param sslKeystore SSL keystore
     */
    private void invokeKeyTool(SSLKeystore sslKeystore) {
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
    }
    
    /**
     * Validates that the necessary parameters have been provided for the specified key/trust store.
     *
     * @param file The file representing the store
     * @param password The password to the store
     * @param storeType identifies whether the store is a KeyStore or TrustStore
     */
    private void validateStore(File file, String password, StoreType storeType) {
        if(!file ^ !password) {
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
}