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

import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.tomcat.embedded.TomcatVersion
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional

/**
 * Deploys an exploded web application to an embedded Tomcat web container. Does not require that the web application
 * be assembled into a war, saving time during the development cycle.
 *
 * @author Benjamin Muschko
 */
class TomcatRun extends AbstractTomcatRun {
    /**
     * The web application's classpath. This classpath usually contains the external libraries assigned to the compile
     * and runtime configurations.
     */
    @InputFiles
    FileCollection webAppClasspath

    /**
     * The web application's source directory. For applications following the Servlet 3.x specification this directory
     * is not required.
     */
    @InputDirectory
    @Optional
    File webAppSourceDirectory

    /**
     * The directory containing the compiled class files. This property is optional as a web application project may
     * simply not contain any classes.
     */
    @InputDirectory
    @Optional
    File classesDirectory

    @Override
    void validateConfiguration() {
        super.validateConfiguration()
        validateConfigFile()
    }

    /**
     * Validate the configuration file.
     */
    private void validateConfigFile() {
        File resolvedWebAppSourceDirectory = getWebAppSourceDirectory()
        logger.info "Webapp source directory = ${resolvedWebAppSourceDirectory?.canonicalPath}"

        if(resolvedWebAppSourceDirectory) {
            File defaultConfigFile = new File(getWebAppSourceDirectory(), "/${org.gradle.api.plugins.tomcat.tasks.AbstractTomcatRun.CONFIG_FILE}")

            // If context.xml wasn't provided, check the default location
            if(!getConfigFile() && defaultConfigFile.exists()){
                setResolvedConfigFile(defaultConfigFile.toURI().toURL())
                logger.info "context.xml = ${getResolvedConfigFile().toString()}"
            }
        }
    }

    @Override
    void setWebApplicationContext() {
        getServer().createContext(getFullContextPath(), getWebAppSourceDirectory()?.canonicalPath)

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
        isTomcat7x() && !existsWebXml()
    }

    /**
     * Checks if web.xml exists in web application source directory.
     *
     * @return Flag
     */
    private boolean existsWebXml() {
        File resolvedWebAppSourceDirectory = getWebAppSourceDirectory()

        if(resolvedWebAppSourceDirectory) {
            File webXml = new File(getWebAppSourceDirectory(), 'WEB-INF/web.xml')
            webXml.exists()
        }

        false
    }

    /**
     * For web applications without web.xml running in Tomcat 7.x we need to enable directory scanning and create
     * a META-INF to recognize it as exploded JAR directory.
     *
     * @see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=52853#c19">Tomcat Bugzilla ticket</a>
     */
    private void setupClassesJarScanning() {
        getServer().context.jarScanner.scanAllDirectories = true
        File resolvedClassesDirectory = getClassesDirectory()

        if(resolvedClassesDirectory) {
            File metaInfDir = new File(resolvedClassesDirectory, 'META-INF')

            if(!metaInfDir.exists()) {
                boolean success = metaInfDir.mkdir()

                if(!success) {
                    logger.warn "Failed to create META-INF directory in classes directory $resolvedClassesDirectory.absolutePath"
                }
            }
        }
    }

    @Override
    void configureWebApplication() {
        super.configureWebApplication()

        logger.info "web app loader classpath = ${getWebAppClasspath().asPath}"
      
        getWebAppClasspath().each { file ->
            addWebappResource(file)
        }
    }
}
