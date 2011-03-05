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

import org.apache.catalina.connector.Connector
import org.apache.catalina.loader.WebappLoader
import org.apache.catalina.startup.Embedded
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.plugins.tomcat.internal.ShutdownMonitor
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.catalina.*

/**
 * Base class for all tasks which deploy a web application to an embedded Tomcat web container.
 *
 * @author Benjamin Muschko
 */
abstract class AbstractTomcatRunTask extends ConventionTask {
    static final Logger logger = LoggerFactory.getLogger(AbstractTomcatRunTask.class);
    protected boolean reloadable
    private String contextPath
    private Integer httpPort
    private Integer stopPort
    private String stopKey
    private File webDefaultXml
    private Embedded server
    private Context context
    private Realm realm
    private Loader loader
    private Iterable<File> additionalRuntimeJars = new ArrayList<File>()
    private String URIEncoding

    abstract void setWebApplicationContext()

    @TaskAction
    protected void start() {
        logger.info "Configuring Tomcat for ${getProject()}"
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
    }

    /**
     * Configures web application
     */
    void configureWebApplication() {
        setWebApplicationContext()
        setLoader(createLoader())

        for(File additionalRuntimeJar : getAdditionalRuntimeJars()) {
            loader.addRepository(additionalRuntimeJar.toURI().toURL().toString())
        }

        getContext().setLoader(loader)
        getContext().setReloadable(reloadable)
        configureDefaultWebXml()
    }

    void startTomcat() {
        try {
            logger.debug "Starting Tomcat Server ..."

            setServer(createServer())
            getServer().setCatalinaHome(getTemporaryDir().getAbsolutePath())
            getServer().setRealm(realm)

            configureWebApplication()
          
            // Create host
            Host localHost = getServer().createHost("localHost", new File(".").getAbsolutePath())
            localHost.addChild(context)

            // Create engine
            addEngineToServer(localHost)

            // Create HTTP connector
            addConnectorToServer()

            // Start server
            getServer().start()

            logger.info "Started Tomcat Server"
            logger.info "The Server is running at http://localhost:${getHttpPort()}${getContext().path}"

            new ShutdownMonitor(getStopPort(), getStopKey()).start()
        }
        catch(Exception e) {
            throw new GradleException("An error occurred starting the Tomcat server.", e)
        }
        finally {
            logger.info "Tomcat server exiting."
        }
    }

    /**
     * Adds engine to server
     *
     * @param localHost host
     */
    void addEngineToServer(Host localHost) {
        Engine engine = getServer().createEngine()

        engine.with {
            setName "localEngine"
            addChild localHost
            setDefaultHost localHost.getName()
        }

        getServer().addEngine(engine)
    }


    /**
     * Adds connector to server 
     */
    void addConnectorToServer() {
        Connector httpConnector = getServer().createConnector((InetAddress) null, getHttpPort(), false)
        httpConnector.setURIEncoding getURIEncoding() ? getURIEncoding() : 'UTF-8'
        getServer().addConnector(httpConnector)
    }

    /**
     * Configures default web XML if provided. Otherwise, set it up programmatically.
     */
    void configureDefaultWebXml() {
        if(getWebDefaultXml()) {
            getContext().setDefaultWebXml(getWebDefaultXml().getAbsolutePath()) 
        }
        else {
            configureDefaultServlet()
            configureJspServlet()
        }
    }

    /**
     * Configures default servlet and adds it to the context.
     */
    void configureDefaultServlet() {
        Wrapper defaultServlet = getContext().createWrapper()

        defaultServlet.with {
            setName "default"
            setServletClass "org.apache.catalina.servlets.DefaultServlet"
            addInitParameter "debug", "0"
            addInitParameter "listings", "false"
            setLoadOnStartup 1
        }

		getContext().with {
            addChild defaultServlet
            addServletMapping "/", "default"
        }
    }

    /**
     * Configures JSP servlet and adds it to the context.
     */
    void configureJspServlet() {
        Wrapper jspServlet = getContext().createWrapper()

        jspServlet.with {
            setName "jsp"
            setServletClass "org.apache.jasper.servlet.JspServlet"
            addInitParameter "fork", "false"
            addInitParameter "xpoweredBy", "false"
            setLoadOnStartup 3
        }

		getContext().with {
            addChild jspServlet
            addServletMapping "*.jsp", "jsp"
            addServletMapping "*.jspx", "jsp"
        }
    }

    Embedded createServer() {
        new Embedded()
    }

    public Embedded getServer() {
        server
    }

    public void setServer(Embedded server) {
        this.server = server
    }

    public Context getContext() {
        context
    }

    public void setContext(Context context) {
        this.context = context
    }

    public Realm getRealm() {
        realm
    }

    public void setRealm(Realm realm) {
        this.realm = realm
    }

    Loader createLoader() {
        new WebappLoader(getClass().getClassLoader())
    }

    public Loader getLoader() {
        loader
    }

    public void setLoader(Loader loader) {
        this.loader = loader
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
}
