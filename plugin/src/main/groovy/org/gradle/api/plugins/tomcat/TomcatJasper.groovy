/*
 * Copyright 2012 the original author or authors.
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

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*

/**
 * Task to run the JSP compiler and turn JSP pages into Java source.
 *
 * @author Benjamin Muschko
 */
class TomcatJasper extends DefaultTask {
    @InputFiles FileCollection classpath
    Boolean validateXml
    @InputDirectory File uriroot
    @OutputFile @Optional File webXmlFragment
    @OutputDirectory File outputDir
    Boolean classdebuginfo
    String compiler
    String compilerSourceVM
    String compilerTargetVM
    Boolean poolingEnabled
    Boolean errorOnUseBeanInvalidClassAttribute
    Boolean genStringAsCharArray
    String ieClassId
    String javaEncoding
    Boolean trimSpaces
    Boolean xpoweredBy
    Boolean addWebXmlMappings

    @TaskAction
    void start() {
        logger.info "Running Jasper for ${getProject()}"
        logger.info "Jasper classpath = ${getClasspath().asPath}"

        ant.taskdef(classname: 'org.apache.jasper.JspC', name: 'jasper', classpath: getClasspath().asPath)
        ant.jasper(getJasperAttributes())
    }

    private getJasperAttributes() {
        def jasperAttributes = ['validateXml': getValidateXml(), 'uriroot': getUriroot(), 'outputDir': getOutputDir(),
                                'classdebuginfo': getClassdebuginfo(), 'compilerSourceVM': getCompilerSourceVM(),
                                'compilerTargetVM': getCompilerTargetVM(), 'poolingEnabled': getPoolingEnabled(),
                                'errorOnUseBeanInvalidClassAttribute': getErrorOnUseBeanInvalidClassAttribute(),
                                'genStringAsCharArray': getGenStringAsCharArray(), 'ieClassId': getIeClassId(),
                                'javaEncoding': getJavaEncoding(), 'trimSpaces': getTrimSpaces(), 'xpoweredBy': getXpoweredBy()]

        if(getWebXmlFragment()) {
            jasperAttributes['webXmlFragment'] = getWebXmlFragment()
        }

        if(getAddWebXmlMappings()) {
            jasperAttributes['addWebXmlMappings'] = getAddWebXmlMappings()
        }

        if(getCompiler()) {
            jasperAttributes['compiler'] = getCompiler()
        }

        jasperAttributes
    }
}
