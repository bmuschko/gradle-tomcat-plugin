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

import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional

/**
 * Deploys an exploded web application to an embedded Tomcat web container. Does not require that the web application
 * be assembled into a war, saving time during the development cycle.
 *
 * @author Benjamin Muschko
 */
class TomcatRun extends AbstractTomcatRunTask {
    @InputFiles FileCollection webAppClasspath
    @InputDirectory File webAppSourceDirectory
    @InputDirectory @Optional File classesDirectory

    @Override
    void validateConfiguration() {
        super.validateConfiguration()

        // Check the location of the static content/JSPs etc.
        try {
            if(!getWebAppSourceDirectory() || !getWebAppSourceDirectory().exists()) {
                throw new InvalidUserDataException('Webapp source directory '
                        + (!getWebAppSourceDirectory() ? 'null' : getWebAppSourceDirectory().canonicalPath)
                        + ' does not exist')
            }
            else {
                logger.info "Webapp source directory = ${getWebAppSourceDirectory().canonicalPath}"
            }
        }
        catch(IOException e) {
            throw new InvalidUserDataException('Webapp source directory does not exist', e)
        }

        File defaultConfigFile = new File(getWebAppSourceDirectory(), "/${CONFIG_FILE}")

        // If context.xml wasn't provided check the default location
        if(!getConfigFile() && defaultConfigFile.exists()){
            setResolvedConfigFile(defaultConfigFile.toURI().toURL())
            logger.info "context.xml = ${getResolvedConfigFile().toString()}"
        }
    }

    @Override
    void setWebApplicationContext() {
        getServer().createContext(getFullContextPath(), getWebAppSourceDirectory().canonicalPath)

        if(isClassesJarScanningRequired()) {
            setupClassesJarScanning()
        }
    }

    /**
     * Checks to see if classes JAR scanning is required.
     *
     * @return Flag
     */
    private boolean isClassesJarScanningRequired() {
        (isTomcat7x() || isTomcat8x()) && !existsWebXml()
    }

    /**
     * Checks if web.xml exists in web application source directory.
     *
     * @return Flag
     */
    private boolean existsWebXml() {
        File webXml = new File(getWebAppSourceDirectory(), 'WEB-INF/web.xml')
        webXml.exists()
    }

    /**
     * For web applications without web.xml running in Tomcat 7.x we need to enable directory scanning and create
     * a META-INF to recognize it as exploded JAR directory.
     *
     * @see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=52853#c19">Tomcat Bugzilla ticket</a>
     */
    private void setupClassesJarScanning() {
        getServer().context.jarScanner.scanAllDirectories = true

        if(getClassesDirectory()) {
            File metaInfDir = new File(getClassesDirectory(), 'META-INF')

            if(!metaInfDir.exists()) {
                boolean success = metaInfDir.mkdir()

                if(!success) {
                    logger.warn "Failed to create META-INF directory in classes directory ${getClassesDirectory().absolutePath}"
                }
            }
        }
    }

    @Override
    void configureWebApplication() {
        super.configureWebApplication()

        logger.info "web app loader classpath = ${getWebAppClasspath().asPath}"

        def type
        if(isTomcat8x()) {
            type = getResourceSetType('POST')
        }

        getWebAppClasspath().each { file ->
            if(isTomcat8x()) {
                if(file.exists()) {
                    getServer().context.resources.createWebResourceSet(type, '/WEB-INF/classes', file.getAbsolutePath(), null, '/')
                }
            }
            else {
                getServer().context.loader.addRepository(file.toURI().toURL().toString())
            }
        }
    }
}
