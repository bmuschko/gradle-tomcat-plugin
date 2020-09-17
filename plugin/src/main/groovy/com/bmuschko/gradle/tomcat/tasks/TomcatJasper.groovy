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
package com.bmuschko.gradle.tomcat.tasks

import com.bmuschko.gradle.tomcat.options.TrimSpaces
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*

import java.nio.file.Path

/**
 * Task to run the JSP compiler and turn JSP pages into Java source.
 */
class TomcatJasper extends Tomcat {
    @InputFiles
    FileCollection classpath

    @Input
    @Optional
    Boolean validateXml

    /**
     * @since 1.2.1
     */
    @Input
    @Optional
    Boolean validateTld

    @InputDirectory
    File uriroot

    @OutputFile
    @Optional
    File webXmlFragment

    @OutputDirectory
    File outputDir

    @Input
    @Optional
    Boolean classdebuginfo

    @Input
    @Optional
    String compiler

    @Input
    @Optional
    String compilerSourceVM

    @Input
    @Optional
    String compilerTargetVM

    @Input
    @Optional
    Boolean poolingEnabled

    @Input
    @Optional
    Boolean errorOnUseBeanInvalidClassAttribute

    @Input
    @Optional
    Boolean genStringAsCharArray

    @Input
    @Optional
    String ieClassId

    @Input
    @Optional
    String javaEncoding

    @Input
    @Optional
    TrimSpaces trimSpaces

    @Input
    @Optional
    Boolean xpoweredBy

    @Input
    @Optional
    Boolean addWebXmlMappings

    @InputFiles
    @Optional
    FileCollection jspFiles

    @TaskAction
    void start() {
        logger.info "Running Jasper for ${getProject()}"
        logger.info "Jasper classpath = ${getClasspath().asPath}"

        ant.taskdef(classname: 'org.apache.jasper.JspC', name: 'jasper', classpath: getClasspath().asPath)
        ant.jasper(getJasperAttributes())
    }

    @Internal('private method, ignore for task validation')
    private getJasperAttributes() {
        def jasperAttributes = ['uriroot': getUriroot(), 'outputDir': getOutputDir(),
                                'classdebuginfo': getClassdebuginfo(), 'compilerSourceVM': getCompilerSourceVM(),
                                'compilerTargetVM': getCompilerTargetVM(), 'poolingEnabled': getPoolingEnabled(),
                                'errorOnUseBeanInvalidClassAttribute': getErrorOnUseBeanInvalidClassAttribute(),
                                'genStringAsCharArray': getGenStringAsCharArray(), 'ieClassId': getIeClassId(),
                                'javaEncoding': getJavaEncoding(), 'trimSpaces': getTrimSpaces()?.name(), 'xpoweredBy': getXpoweredBy()]
        if(getValidateXml()) {
            jasperAttributes['validateXml'] = getValidateXml()
        }

        if(getValidateTld()) {
            jasperAttributes['validateTld'] = getValidateTld()
        }

        if(getWebXmlFragment()) {
            jasperAttributes['webXmlFragment'] = getWebXmlFragment()
        }

        if(getAddWebXmlMappings()) {
            jasperAttributes['addWebXmlMappings'] = getAddWebXmlMappings()
        }

        if(getCompiler()) {
            jasperAttributes['compiler'] = getCompiler()
        }

        if(getJspFiles()) {
            // process filecollection into a comma-separated list of relative paths from uriroot
            StringBuilder fileList = new StringBuilder()
            Path basePath = getUriroot().toPath()
            getJspFiles().each({ file ->
                fileList.append(basePath.relativize(file.toPath()).toString())
                fileList.append(',')
            })
            if(fileList.length() > 0) {
                jasperAttributes['jspFiles'] = fileList.substring(0, fileList.length() - 1).toString()
            }
        }

        jasperAttributes
    }
}
