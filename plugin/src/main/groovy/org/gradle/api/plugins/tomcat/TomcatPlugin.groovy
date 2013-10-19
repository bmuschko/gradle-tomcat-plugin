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
    static final String TOMCAT_RUN_TASK_NAME = 'tomcatRun'
    static final String TOMCAT_RUN_WAR_TASK_NAME = 'tomcatRunWar'
    static final String TOMCAT_STOP_TASK_NAME = 'tomcatStop'
    static final String TOMCAT_JASPER_TASK_NAME = 'tomcatJasper'
    static final String CLASSPATH = 'classpath'
    static final String HTTP_PORT_CONVENTION = 'httpPort'
    static final String HTTPS_PORT_CONVENTION = 'httpsPort'
    static final String STOP_PORT_CONVENTION = 'stopPort'
    static final String STOP_KEY_CONVENTION = 'stopKey'
    static final String ENABLE_SSL_CONVENTION = 'enableSSL'
    static final String HTTP_PROTOCOL = 'httpProtocol'
    static final String HTTPS_PROTOCOL = 'httpsProtocol'
    static final String TOMCAT_CONFIGURATION_NAME = 'tomcat'
    static final String AJP_PORT_CONVENTION = 'ajpPort'
    static final String AJP_PROTOCOL_CONVENTION = 'ajpProtocol'

    @Override
    void apply(Project project) {
        project.plugins.apply(WarPlugin)

        project.configurations.create(TOMCAT_CONFIGURATION_NAME).setVisible(false).setTransitive(true)
               .setDescription('The Tomcat libraries to be used for this project.')

        TomcatPluginConvention tomcatConvention = new TomcatPluginConvention()
        project.convention.plugins.tomcat = tomcatConvention

        configureMappingRules(project, tomcatConvention)
        configureTomcatRun(project)
        configureTomcatRunWar(project)
        configureTomcatStop(project, tomcatConvention)
        configureJasper(project, tomcatConvention)
    }

    private void configureMappingRules(final Project project, final TomcatPluginConvention tomcatConvention) {
        project.tasks.withType(AbstractTomcatRunTask).whenTaskAdded { AbstractTomcatRunTask abstractTomcatRunTask ->
            configureAbstractTomcatTask(project, tomcatConvention, abstractTomcatRunTask)        
        }
    }

    private void configureAbstractTomcatTask(final Project project, final TomcatPluginConvention tomcatConvention, AbstractTomcatRunTask tomcatTask) {
        tomcatTask.daemon = false
        tomcatTask.reloadable = true
        tomcatTask.conventionMapping.map('buildscriptClasspath') { project.buildscript.configurations.getByName(CLASSPATH).asFileTree }
        tomcatTask.conventionMapping.map('tomcatClasspath') { project.configurations.getByName(TOMCAT_CONFIGURATION_NAME).asFileTree }
        tomcatTask.conventionMapping.map('contextPath') { project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName }
        tomcatTask.conventionMapping.map(HTTP_PORT_CONVENTION) { tomcatConvention.httpPort }
        tomcatTask.conventionMapping.map(HTTPS_PORT_CONVENTION) { tomcatConvention.httpsPort }
        tomcatTask.conventionMapping.map(STOP_PORT_CONVENTION) { tomcatConvention.stopPort }
        tomcatTask.conventionMapping.map(STOP_KEY_CONVENTION) { tomcatConvention.stopKey }
        tomcatTask.conventionMapping.map(ENABLE_SSL_CONVENTION) { tomcatConvention.enableSSL }
        tomcatTask.conventionMapping.map(HTTP_PROTOCOL) { tomcatConvention.httpProtocol }
        tomcatTask.conventionMapping.map(HTTPS_PROTOCOL) { tomcatConvention.httpsProtocol }
        tomcatTask.conventionMapping.map(AJP_PORT_CONVENTION) { tomcatConvention.ajpPort }
        tomcatTask.conventionMapping.map(AJP_PROTOCOL_CONVENTION) { tomcatConvention.ajpProtocol }
    }

    private void configureTomcatRun(final Project project) {
        project.tasks.withType(TomcatRun).whenTaskAdded { TomcatRun tomcatRun ->
            tomcatRun.conventionMapping.map('webAppClasspath') { project.tasks.getByName(WarPlugin.WAR_TASK_NAME).classpath }
            tomcatRun.conventionMapping.map('webAppSourceDirectory') { getWarConvention(project).webAppDir }
            tomcatRun.conventionMapping.map('classesDirectory') { project.sourceSets.main.output.classesDir }
        }

        TomcatRun tomcatRun = project.tasks.create(TOMCAT_RUN_TASK_NAME, TomcatRun)
        tomcatRun.description = 'Uses your files as and where they are and deploys them to Tomcat.'
        tomcatRun.group = WarPlugin.WEB_APP_GROUP
    }

    private void configureTomcatRunWar(final Project project) {
        project.tasks.withType(TomcatRunWar).whenTaskAdded { TomcatRunWar tomcatRunWar ->
            tomcatRunWar.dependsOn(WarPlugin.WAR_TASK_NAME)
            tomcatRunWar.conventionMapping.map('webApp') { project.tasks.getByName(WarPlugin.WAR_TASK_NAME).archivePath }
        }

        TomcatRunWar tomcatRunWar = project.tasks.create(TOMCAT_RUN_WAR_TASK_NAME, TomcatRunWar)
        tomcatRunWar.description = 'Assembles the webapp into a war and deploys it to Tomcat.'
        tomcatRunWar.group = WarPlugin.WEB_APP_GROUP
    }

    private void configureTomcatStop(final Project project, final TomcatPluginConvention tomcatConvention) {
        TomcatStop tomcatStop = project.tasks.create(TOMCAT_STOP_TASK_NAME, TomcatStop)
        tomcatStop.description = 'Stops Tomcat.'
        tomcatStop.group = WarPlugin.WEB_APP_GROUP
        tomcatStop.conventionMapping.map(STOP_PORT_CONVENTION) { tomcatConvention.stopPort }
        tomcatStop.conventionMapping.map(STOP_KEY_CONVENTION) { tomcatConvention.stopKey }
    }

    private void configureJasper(final Project project, final TomcatPluginConvention tomcatConvention) {
        project.tasks.withType(TomcatJasper).whenTaskAdded { TomcatJasper tomcatJasperTask ->
            tomcatJasperTask.conventionMapping.map('classpath') { project.configurations.getByName(TOMCAT_CONFIGURATION_NAME).asFileTree + project.tasks.getByName(WarPlugin.WAR_TASK_NAME).classpath }
            tomcatJasperTask.conventionMapping.map('validateXml') { tomcatConvention.jasper.validateXml ?: false }
            tomcatJasperTask.conventionMapping.map('uriroot') { tomcatConvention.jasper.uriroot ?: project.webAppDir }
            tomcatJasperTask.conventionMapping.map('webXmlFragment') { tomcatConvention.jasper.webXmlFragment }
            tomcatJasperTask.conventionMapping.map('addWebXmlMappings') { tomcatConvention.jasper.addWebXmlMappings }
            tomcatJasperTask.conventionMapping.map('outputDir') { tomcatConvention.jasper.outputDir ?: new File(project.buildDir, 'jasper') }
            tomcatJasperTask.conventionMapping.map('classdebuginfo') { tomcatConvention.jasper.classdebuginfo ?: true }
            tomcatJasperTask.conventionMapping.map('compiler') { tomcatConvention.jasper.compiler }
            tomcatJasperTask.conventionMapping.map('compilerSourceVM') { tomcatConvention.jasper.compilerSourceVM ?: '1.6' }
            tomcatJasperTask.conventionMapping.map('compilerTargetVM') { tomcatConvention.jasper.compilerTargetVM ?: '1.6' }
            tomcatJasperTask.conventionMapping.map('poolingEnabled') { tomcatConvention.jasper.poolingEnabled ?: true }
            tomcatJasperTask.conventionMapping.map('errorOnUseBeanInvalidClassAttribute') { tomcatConvention.jasper.errorOnUseBeanInvalidClassAttribute ?: true }
            tomcatJasperTask.conventionMapping.map('genStringAsCharArray') { tomcatConvention.jasper.genStringAsCharArray ?: false }
            tomcatJasperTask.conventionMapping.map('ieClassId') { tomcatConvention.jasper.ieClassId ?: 'clsid:8AD9C840-044E-11D1-B3E9-00805F499D93' }
            tomcatJasperTask.conventionMapping.map('javaEncoding') { tomcatConvention.jasper.javaEncoding ?: 'UTF8' }
            tomcatJasperTask.conventionMapping.map('trimSpaces') { tomcatConvention.jasper.trimSpaces ?: false }
            tomcatJasperTask.conventionMapping.map('xpoweredBy') { tomcatConvention.jasper.xpoweredBy ?: false }
        }

        TomcatJasper tomcatJasper = project.tasks.create(TOMCAT_JASPER_TASK_NAME, TomcatJasper)
        tomcatJasper.description = 'Runs the JSP compiler and turns JSP pages into Java source.'
        tomcatJasper.group = WarPlugin.WEB_APP_GROUP
    }

    WarPluginConvention getWarConvention(Project project) {
        project.convention.getPlugin(WarPluginConvention)
    }
}
