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

import org.apache.catalina.loader.WebappLoader
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Deploys an exploded web application to an embedded Tomcat web container. Does not require that the web application
 * be assembled into a war, saving time during the development cycle.
 *
 * @author Benjamin Muschko
 */
class TomcatRun extends AbstractTomcatRunTask {
    static Logger logger = LoggerFactory.getLogger(TomcatRun.class)
    private FileCollection classpath
    private File webAppSourceDirectory

    @Override
    void validateConfiguration() {
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

    @Override
    void configureWebApplication() {
        WebappLoader loader = new WebappLoader(getClass().getClassLoader())

        getClasspath().each { file ->
            loader.addRepository(file.toURI().toURL().toString())
        }
      
        setContext(getServer().createContext("/" + getContextPath(), getWebAppSourceDirectory().getCanonicalPath()))
        getContext().setLoader(loader)
        getContext().setReloadable(reloadable)
        configureDefaultWebXml()
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
}
