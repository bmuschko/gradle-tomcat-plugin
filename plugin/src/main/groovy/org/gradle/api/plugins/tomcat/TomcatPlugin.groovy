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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.WarPluginConvention

/**
 * <p>A {@link Plugin} which extends the {@link WarPlugin} to add tasks which run the web application using an embedded
 * Tomcat web container.</p>
 *
 * @author Benjamin Muschko
 */
class TomcatPlugin implements Plugin<Project> {
    static final String TOMCAT_CONFIGURATION_NAME = 'tomcat'
    static final String TOMCAT_RUN_TASK_NAME = 'tomcatRun'
    static final String TOMCAT_RUN_WAR_TASK_NAME = 'tomcatRunWar'
    static final String TOMCAT_STOP_TASK_NAME = 'tomcatStop'
    static final String TOMCAT_JASPER_TASK_NAME = 'tomcatJasper'
    static final String STOP_PORT_CONVENTION = 'stopPort'
    static final String STOP_KEY_CONVENTION = 'stopKey'

    @Override
    void apply(Project project) {
        project.plugins.apply(WarPlugin)

        project.configurations.create(TOMCAT_CONFIGURATION_NAME).setVisible(false).setTransitive(true)
               .setDescription('The Tomcat libraries to be used for this project.')

        TomcatPluginConvention tomcatConvention = new TomcatPluginConvention()
        project.convention.plugins.tomcat = tomcatConvention

        configureAbstractTomcatTask(project, tomcatConvention)
        configureTomcatRun(project)
        configureTomcatRunWar(project)
        configureTomcatStop(project, tomcatConvention)
        configureJasper(project, tomcatConvention)
    }

    private void configureAbstractTomcatTask(final Project project, final TomcatPluginConvention tomcatConvention) {
        project.tasks.withType(AbstractTomcatRun) {
            conventionMapping.map('buildscriptClasspath') { project.buildscript.configurations.getByName('classpath').asFileTree }
            conventionMapping.map('tomcatClasspath') { project.configurations.getByName(TOMCAT_CONFIGURATION_NAME).asFileTree }
            conventionMapping.map('contextPath') { project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName }
            conventionMapping.map('httpPort') { tomcatConvention.httpPort }
            conventionMapping.map('httpsPort') { tomcatConvention.httpsPort }
            conventionMapping.map(STOP_PORT_CONVENTION) { tomcatConvention.stopPort }
            conventionMapping.map(STOP_KEY_CONVENTION) { tomcatConvention.stopKey }
            conventionMapping.map('enableSSL') { tomcatConvention.enableSSL }
            conventionMapping.map('httpProtocol') { tomcatConvention.httpProtocol }
            conventionMapping.map('httpsProtocol') { tomcatConvention.httpsProtocol }
            conventionMapping.map('ajpPort') { tomcatConvention.ajpPort }
            conventionMapping.map('ajpProtocol') { tomcatConvention.ajpProtocol }
        }
    }

    private void configureTomcatRun(final Project project) {
        project.tasks.create(TOMCAT_RUN_TASK_NAME, TomcatRun) {
            description = 'Uses your files as and where they are and deploys them to Tomcat.'
            conventionMapping.map('webAppClasspath') { project.tasks.getByName(WarPlugin.WAR_TASK_NAME).classpath }
            conventionMapping.map('webAppSourceDirectory') {
                File webAppDir = getWarConvention(project).webAppDir
                webAppDir.exists() ? webAppDir : null
            }
            conventionMapping.map('classesDirectory') { project.sourceSets.main.output.classesDir.exists() ? project.sourceSets.main.output.classesDir : null }
        }
    }

    private void configureTomcatRunWar(final Project project) {
        project.tasks.create(TOMCAT_RUN_WAR_TASK_NAME, TomcatRunWar) {
            description = 'Assembles the webapp into a war and deploys it to Tomcat.'
            dependsOn WarPlugin.WAR_TASK_NAME
            conventionMapping.map('webApp') { project.tasks.getByName(WarPlugin.WAR_TASK_NAME).archivePath }
        }
    }

    private void configureTomcatStop(final Project project, final TomcatPluginConvention tomcatConvention) {
        project.tasks.create(TOMCAT_STOP_TASK_NAME, TomcatStop) {
            description = 'Stops Tomcat.'
            conventionMapping.map(STOP_PORT_CONVENTION) { tomcatConvention.stopPort }
            conventionMapping.map(STOP_KEY_CONVENTION) { tomcatConvention.stopKey }
        }
    }

    private void configureJasper(final Project project, final TomcatPluginConvention tomcatConvention) {
        project.tasks.create(TOMCAT_JASPER_TASK_NAME, TomcatJasper) {
            description = 'Runs the JSP compiler and turns JSP pages into Java source.'
            conventionMapping.map('classpath') { project.configurations.getByName(TOMCAT_CONFIGURATION_NAME).asFileTree + project.tasks.getByName(WarPlugin.WAR_TASK_NAME).classpath }
            conventionMapping.map('validateXml') { tomcatConvention.jasper.validateXml ?: false }
            conventionMapping.map('uriroot') { tomcatConvention.jasper.uriroot ?: project.webAppDir }
            conventionMapping.map('webXmlFragment') { tomcatConvention.jasper.webXmlFragment }
            conventionMapping.map('addWebXmlMappings') { tomcatConvention.jasper.addWebXmlMappings }
            conventionMapping.map('outputDir') { tomcatConvention.jasper.outputDir ?: new File(project.buildDir, 'jasper') }
            conventionMapping.map('classdebuginfo') { tomcatConvention.jasper.classdebuginfo ?: true }
            conventionMapping.map('compiler') { tomcatConvention.jasper.compiler }
            conventionMapping.map('compilerSourceVM') { tomcatConvention.jasper.compilerSourceVM ?: '1.6' }
            conventionMapping.map('compilerTargetVM') { tomcatConvention.jasper.compilerTargetVM ?: '1.6' }
            conventionMapping.map('poolingEnabled') { tomcatConvention.jasper.poolingEnabled ?: true }
            conventionMapping.map('errorOnUseBeanInvalidClassAttribute') { tomcatConvention.jasper.errorOnUseBeanInvalidClassAttribute ?: true }
            conventionMapping.map('genStringAsCharArray') { tomcatConvention.jasper.genStringAsCharArray ?: false }
            conventionMapping.map('ieClassId') { tomcatConvention.jasper.ieClassId ?: 'clsid:8AD9C840-044E-11D1-B3E9-00805F499D93' }
            conventionMapping.map('javaEncoding') { tomcatConvention.jasper.javaEncoding ?: 'UTF8' }
            conventionMapping.map('trimSpaces') { tomcatConvention.jasper.trimSpaces ?: false }
            conventionMapping.map('xpoweredBy') { tomcatConvention.jasper.xpoweredBy ?: false }
        }
    }

    private WarPluginConvention getWarConvention(Project project) {
        project.convention.getPlugin(WarPluginConvention)
    }
}
