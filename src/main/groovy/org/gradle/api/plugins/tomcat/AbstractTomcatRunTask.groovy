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
import org.gradle.api.plugins.tomcat.internal.ShutdownMonitor
import org.gradle.api.plugins.tomcat.internal.TomcatServerFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base class for all tasks which deploy a web application to an embedded Tomcat web container.
 *
 * @author Benjamin Muschko
 */
abstract class AbstractTomcatRunTask extends ConventionTask {
    static final Logger logger = LoggerFactory.getLogger(AbstractTomcatRunTask.class)
    protected boolean reloadable
    private String contextPath
    private Integer httpPort
    private Integer stopPort
    private String stopKey
    private File webDefaultXml
    def server
    private Realm realm
    private Iterable<File> additionalRuntimeJars = new ArrayList<File>()
    private String URIEncoding
    private boolean daemon
    private FileCollection serverClasspath
    private File configFile

    abstract void setWebApplicationContext()

    @TaskAction
    protected void start() {
        logger.info "Configuring Tomcat for ${getProject()}"

        ClassLoader originalClassLoader = getClass().getClassLoader()
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
        URL[] runtimeClasspath = classPathRegistry.getClassPathUrls("GRADLE_RUNTIME")
        ClassLoader rootClassLoader = ClassLoader.getSystemClassLoader().getParent()
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
                throw new InvalidUserDataException("The provided default web.xml file does not exist")
            }
            else {
                logger.info "Default web.xml = ${getWebDefaultXml().getCanonicalPath()}"
            }
        }

        // Check the location of context.xml if it was provided.
        if(getConfigFile()) {
            if(!getConfigFile().exists()) {
                throw new InvalidUserDataException("context.xml file does not exist at location ${getConfigFile().getCanonicalPath()}")
            }
            else {
                logger.info "context.xml = ${getConfigFile().getCanonicalPath()}"
            }
        }
    }

    /**
     * Configures web application
     */
    void configureWebApplication() {
        setWebApplicationContext()
        getServer().createLoader(Thread.currentThread().getContextClassLoader())

        for(File additionalRuntimeJar : getAdditionalRuntimeJars()) {
            getServer().getLoader().addRepository(additionalRuntimeJar.toURI().toURL().toString())
        }

        getServer().getContext().setLoader(getServer().getLoader())
        getServer().getContext().setReloadable(reloadable)
        getServer().configureDefaultWebXml(getWebDefaultXml())
        getServer().setConfigFile(getConfigFile())
    }

    void startTomcat() {
        try {
            logger.debug "Starting Tomcat Server ..."

            setServer(createServer())
            getServer().setHome(getTemporaryDir().getAbsolutePath())
            getServer().setRealm(realm)

            configureWebApplication()

            getServer().configureContainer(getHttpPort(), getURIEncoding())

            // Start server
            getServer().start()

            logger.info "Started Tomcat Server"
            logger.info "The Server is running at http://localhost:${getHttpPort()}${getServer().getContext().path}"

            Thread shutdownMonitor = new ShutdownMonitor(getStopPort(), getStopKey(), getServer(), daemon)
            shutdownMonitor.start()

            if(!daemon) {
                shutdownMonitor.join()
            }
        }
        catch(Exception e) {
            throw new GradleException("An error occurred starting the Tomcat server.", e)
        }
        finally {
            if(!daemon) {
                logger.info "Tomcat server exiting."
            }
        }
    }

    String getFullContextPath() {
        getContextPath().startsWith("/") ? getContextPath() : "/" + getContextPath()
    }

    def createServer() {
        TomcatServerFactory.instance.tomcatServer
    }

    def getServer() {
        server
    }

    def setServer(server) {
        this.server = server
    }

    public Realm getRealm() {
        realm
    }

    public void setRealm(Realm realm) {
        this.realm = realm
    }

    public String getContextPath() {
        contextPath
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public Integer getHttpPort() {
        Integer httpPortSystemProperty = TomcatSystemProperty.getHttpPort()
        httpPortSystemProperty ? httpPortSystemProperty : httpPort
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort
    }

    public Integer getStopPort() {
        Integer stopPortSystemProperty = TomcatSystemProperty.getStopPort()
        stopPortSystemProperty ? stopPortSystemProperty : stopPort
    }

    public void setStopPort(Integer stopPort) {
        this.stopPort = stopPort
    }

    public String getStopKey() {
        String stopKeySystemProperty = TomcatSystemProperty.getStopKey()
        stopKeySystemProperty ? stopKeySystemProperty : stopKey
    }

    public void setStopKey(String stopKey) {
        this.stopKey = stopKey
    }

    @InputFile
    @Optional
    public File getWebDefaultXml() {
        webDefaultXml
    }

    public void setWebDefaultXml(File webDefaultXml) {
        this.webDefaultXml = webDefaultXml
    }

    @InputFiles
    public Iterable<File> getAdditionalRuntimeJars() {
        additionalRuntimeJars
    }

    public void setAdditionalRuntimeJars(Iterable<File> additionalRuntimeJars) {
        this.additionalRuntimeJars = additionalRuntimeJars
    }

    @Optional
    public String getURIEncoding() {
        URIEncoding
    }

    public void setURIEncoding(String URIEncoding) {
        this.URIEncoding = URIEncoding
    }

    public boolean isDaemon() {
        daemon
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon
    }

    public FileCollection getServerClasspath() {
        serverClasspath
    }

    public void setServerClasspath(FileCollection serverClasspath) {
        this.serverClasspath = serverClasspath
    }

    @InputFile
    @Optional
    public File getConfigFile() {
        configFile
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile
    }
}
