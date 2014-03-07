/*
 * Copyright 2014 the original author or authors.
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.tomcat.tasks.AbstractTomcatRun
import org.gradle.api.plugins.tomcat.tasks.TomcatJasper
import org.gradle.api.plugins.tomcat.tasks.TomcatRun
import org.gradle.api.plugins.tomcat.tasks.TomcatRunWar

/**
 * <p>A {@link Plugin} which applies the {@link WarPlugin} and provides tasks for managing a web application using an embedded
 * Tomcat web container.</p>
 *
 * @author Benjamin Muschko
 */
class TomcatBasePlugin implements Plugin<Project> {
    static final String TOMCAT_CONFIGURATION_NAME = 'tomcat'

    @Override
    void apply(Project project) {
        project.plugins.apply(WarPlugin)
        project.configurations.create(TOMCAT_CONFIGURATION_NAME).setVisible(false).setTransitive(true)
               .setDescription('The Tomcat libraries to be used for this project.')

        configureAbstractTomcatTask(project)
        configureTomcatRunTask(project)
        configureTomcatRunWarTask(project)
        configureTomcatJasperTask(project)
    }

    private void configureAbstractTomcatTask(Project project) {
        project.tasks.withType(AbstractTomcatRun) {
            conventionMapping.map('buildscriptClasspath') { project.buildscript.configurations.getByName('classpath').asFileTree }
            conventionMapping.map('tomcatClasspath') { project.configurations.getByName(TOMCAT_CONFIGURATION_NAME).asFileTree }
            conventionMapping.map('contextPath') { project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName }
        }
    }

    private void configureTomcatRunTask(Project project) {
        project.tasks.withType(TomcatRun) {
            conventionMapping.map('webAppClasspath') { project.tasks.getByName(WarPlugin.WAR_TASK_NAME).classpath }
        }
    }

    private void configureTomcatRunWarTask(Project project) {
        project.tasks.withType(TomcatRunWar) {
            dependsOn WarPlugin.WAR_TASK_NAME
            conventionMapping.map('webApp') { project.tasks.getByName(WarPlugin.WAR_TASK_NAME).archivePath }
        }
    }

    private void configureTomcatJasperTask(Project project) {
        project.tasks.withType(TomcatJasper) {
            conventionMapping.map('classpath') { project.configurations.getByName(TOMCAT_CONFIGURATION_NAME).asFileTree + project.tasks.getByName(WarPlugin.WAR_TASK_NAME).classpath }
        }
    }
}
