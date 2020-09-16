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
package com.bmuschko.gradle.tomcat

import com.bmuschko.gradle.tomcat.options.TrimSpaces
import com.bmuschko.gradle.tomcat.tasks.AbstractTomcatRun
import com.bmuschko.gradle.tomcat.tasks.TomcatJasper
import com.bmuschko.gradle.tomcat.tasks.TomcatRun
import com.bmuschko.gradle.tomcat.tasks.TomcatRunWar
import com.bmuschko.gradle.tomcat.tasks.TomcatStop
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.bmuschko.gradle.tomcat.extension.TomcatPluginExtension
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.tomcat.tasks.*

/**
 * <p>A {@link Plugin} which applies the {@link TomcatBasePlugin} and creates preconfigured tasks which run the web
 * application using an embedded Tomcat web container.</p>
 */
class TomcatPlugin implements Plugin<Project> {
    static final String TOMCAT_EXTENSION_NAME = 'tomcat'
    static final String TOMCAT_RUN_TASK_NAME = 'tomcatRun'
    static final String TOMCAT_RUN_WAR_TASK_NAME = 'tomcatRunWar'
    static final String TOMCAT_STOP_TASK_NAME = 'tomcatStop'
    static final String TOMCAT_JASPER_TASK_NAME = 'tomcatJasper'
    static final String STOP_PORT_CONVENTION = 'stopPort'
    static final String STOP_KEY_CONVENTION = 'stopKey'

    @Override
    void apply(Project project) {
        project.plugins.apply(TomcatBasePlugin)
        TomcatPluginExtension tomcatPluginExtension = project.extensions.create(TOMCAT_EXTENSION_NAME, TomcatPluginExtension)

        configureAbstractTomcatTask(project, tomcatPluginExtension)
        configureTomcatRun(project)
        configureTomcatRunWar(project)
        configureTomcatStop(project, tomcatPluginExtension)
        configureJasper(project, tomcatPluginExtension)
    }

    private void configureAbstractTomcatTask(Project project, TomcatPluginExtension tomcatPluginExtension) {
        project.tasks.withType(AbstractTomcatRun) {
            conventionMapping.map('httpPort') { tomcatPluginExtension.httpPort }
            conventionMapping.map('httpsPort') { tomcatPluginExtension.httpsPort }
            conventionMapping.map(STOP_PORT_CONVENTION) { tomcatPluginExtension.stopPort }
            conventionMapping.map(STOP_KEY_CONVENTION) { tomcatPluginExtension.stopKey }
            conventionMapping.map('contextPath') { tomcatPluginExtension.contextPath ?: project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName }
            conventionMapping.map('enableSSL') { tomcatPluginExtension.enableSSL }
            conventionMapping.map('daemon') { tomcatPluginExtension.daemon }
            conventionMapping.map('httpProtocol') { tomcatPluginExtension.httpProtocol }
            conventionMapping.map('httpsProtocol') { tomcatPluginExtension.httpsProtocol }
            conventionMapping.map('ajpPort') { tomcatPluginExtension.ajpPort }
            conventionMapping.map('ajpProtocol') { tomcatPluginExtension.ajpProtocol }
            conventionMapping.map('users') { tomcatPluginExtension.users }
        }
    }

    private void configureTomcatRun(Project project) {
        project.tasks.create(TOMCAT_RUN_TASK_NAME, TomcatRun) {
            description = 'Uses your files as and where they are and deploys them to Tomcat.'
        }
    }

    private void configureTomcatRunWar(Project project) {
        project.tasks.create(TOMCAT_RUN_WAR_TASK_NAME, TomcatRunWar) {
            description = 'Assembles the webapp into a war and deploys it to Tomcat.'
        }
    }

    private void configureTomcatStop(Project project, TomcatPluginExtension tomcatPluginExtension) {
        project.tasks.create(TOMCAT_STOP_TASK_NAME, TomcatStop) {
            description = 'Stops Tomcat.'
            conventionMapping.map(STOP_PORT_CONVENTION) { tomcatPluginExtension.stopPort }
            conventionMapping.map(STOP_KEY_CONVENTION) { tomcatPluginExtension.stopKey }
        }
    }

    private void configureJasper(Project project, TomcatPluginExtension tomcatPluginExtension) {
        project.tasks.create(TOMCAT_JASPER_TASK_NAME, TomcatJasper) {
            description = 'Runs the JSP compiler and turns JSP pages into Java source.'
            conventionMapping.map('validateXml') { tomcatPluginExtension.jasper.validateXml }
            conventionMapping.map('validateTld') { tomcatPluginExtension.jasper.validateTld }
            conventionMapping.map('uriroot') { tomcatPluginExtension.jasper.uriroot ?: project.webAppDir }
            conventionMapping.map('webXmlFragment') { tomcatPluginExtension.jasper.webXmlFragment }
            conventionMapping.map('addWebXmlMappings') { tomcatPluginExtension.jasper.addWebXmlMappings }
            conventionMapping.map('outputDir') { tomcatPluginExtension.jasper.outputDir ?: new File(project.buildDir, 'jasper') }
            conventionMapping.map('classdebuginfo') { tomcatPluginExtension.jasper.classdebuginfo ?: true }
            conventionMapping.map('compiler') { tomcatPluginExtension.jasper.compiler }
            conventionMapping.map('compilerSourceVM') { tomcatPluginExtension.jasper.compilerSourceVM ?: '1.6' }
            conventionMapping.map('compilerTargetVM') { tomcatPluginExtension.jasper.compilerTargetVM ?: '1.6' }
            conventionMapping.map('poolingEnabled') { tomcatPluginExtension.jasper.poolingEnabled ?: true }
            conventionMapping.map('errorOnUseBeanInvalidClassAttribute') { tomcatPluginExtension.jasper.errorOnUseBeanInvalidClassAttribute ?: true }
            conventionMapping.map('genStringAsCharArray') { tomcatPluginExtension.jasper.genStringAsCharArray ?: false }
            conventionMapping.map('ieClassId') { tomcatPluginExtension.jasper.ieClassId ?: 'clsid:8AD9C840-044E-11D1-B3E9-00805F499D93' }
            conventionMapping.map('javaEncoding') { tomcatPluginExtension.jasper.javaEncoding ?: 'UTF8' }
            conventionMapping.map('trimSpaces') { tomcatPluginExtension.jasper.trimSpaces ?: TrimSpaces.TRUE }
            conventionMapping.map('xpoweredBy') { tomcatPluginExtension.jasper.xpoweredBy ?: false }
            conventionMapping.map('jspFiles') { tomcatPluginExtension.jasper.jspFiles ?: null }
        }
    }
}
