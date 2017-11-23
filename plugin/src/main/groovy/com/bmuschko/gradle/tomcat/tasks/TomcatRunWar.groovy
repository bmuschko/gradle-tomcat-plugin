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

import org.gradle.api.tasks.InputFile

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Deploys a WAR to an embedded Tomcat web container.
 */
class TomcatRunWar extends AbstractTomcatRun {
    /**
     * The web archive file used for deployment.
     */
    @InputFile
    File webApp

    @Override
    protected void validateConfiguration() {
        super.validateConfiguration()
        validateConfigFile()
    }

    /**
     * Validate the configuration file.
     */
    private void validateConfigFile() {
        logger.info "Web application WAR = ${getWebApp().canonicalPath}"

        JarFile war = new JarFile(getWebApp())
        JarEntry defaultConfigFileEntry = war.getJarEntry(AbstractTomcatRun.CONFIG_FILE)

        // If context.xml wasn't provided, check the default location
        if(!getConfigFile() && defaultConfigFileEntry) {
            setResolvedConfigFile(new URL("jar:${getWebApp().toURI().toString()}!/${AbstractTomcatRun.CONFIG_FILE}"))
            logger.info "context.xml = ${getResolvedConfigFile().toString()}"
        }
    }

    @Override
    void setWebApplicationContext() {
        getServer().createContext(getFullContextPath(), getWebApp().canonicalPath)
    }
}
