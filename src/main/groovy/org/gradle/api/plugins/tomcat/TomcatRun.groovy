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

import org.apache.catalina.Context
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Deploys an exploded web application to an embedded Tomcat web container. Does not require that the web application
 * be assembled into a war, saving time during the development cycle.
 *
 * @author Benjamin Muschko
 */
class TomcatRun extends AbstractTomcatRunTask {
    static final Logger logger = LoggerFactory.getLogger(TomcatRun.class)
    private FileCollection classpath
    private File webAppSourceDirectory
    private File configFile

    @Override
    void validateConfiguration() {
        super.validateConfiguration()

        // Check the location of the static content/JSPs etc.
        try {
            if(!getWebAppSourceDirectory() || !getWebAppSourceDirectory().exists()) {
                throw new InvalidUserDataException("Webapp source directory "
                        + (getWebAppSourceDirectory() == null ? "null" : getWebAppSourceDirectory().getCanonicalPath())
                        + " does not exist")
            }
            else {
                logger.info "Webapp source directory = ${getWebAppSourceDirectory().getCanonicalPath()}"
            }
        }
        catch(IOException e) {
            throw new InvalidUserDataException("Webapp source directory does not exist", e)
        }


        File defaultConfigFile = new File(getWebAppSourceDirectory(), "/META-INF/context.xml")

        // Check the location of context.xml if it was provided.
        if(getConfigFile()) {
            if(!getConfigFile().exists()) {
                throw new InvalidUserDataException("context.xml file does not exist at location ${getConfigFile().getCanonicalPath()}")
            }
            else {
                logger.info "context.xml = ${getConfigFile().getCanonicalPath()}"
            }
        }
        // If context.xml wasn't provided check the default location
        else if(defaultConfigFile.exists()){
            setConfigFile(defaultConfigFile)
            logger.info "context.xml = ${getConfigFile().getCanonicalPath()}"
        }
    }

    @Override
    void setWebApplicationContext() {
        String fullContextPath = getContextPath().startsWith("/") ? getContextPath() : "/" + getContextPath()
        Context context = getServer().createContext(fullContextPath, getWebAppSourceDirectory().getCanonicalPath())

        if(getConfigFile()) {
            context.setConfigFile(getConfigFile().getCanonicalPath())
        }

        setContext(context)
    }

    @Override
    void configureWebApplication() {
        super.configureWebApplication()

        logger.info "classpath = " + getClasspath().asPath
      
        getClasspath().each { file ->
            getLoader().addRepository(file.toURI().toURL().toString())
        }
    }

    @InputFiles
    public FileCollection getClasspath() {
        classpath
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath
    }

    @InputDirectory
    public File getWebAppSourceDirectory() {
        webAppSourceDirectory
    }

    public void setWebAppSourceDirectory(File webAppSourceDirectory) {
        this.webAppSourceDirectory = webAppSourceDirectory
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
