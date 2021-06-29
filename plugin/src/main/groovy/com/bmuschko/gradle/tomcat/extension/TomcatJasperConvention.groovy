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
package com.bmuschko.gradle.tomcat.extension

import com.bmuschko.gradle.tomcat.options.TrimSpaces
import org.gradle.api.file.FileCollection

/**
 * Defines Jasper task convention.
 */
class TomcatJasperConvention {
    Boolean validateXml
    Boolean validateTld
    File uriroot
    File webXmlFragment
    File outputDir
    Boolean classdebuginfo
    String compiler
    String compilerSourceVM
    String compilerTargetVM
    Boolean poolingEnabled
    Boolean errorOnUseBeanInvalidClassAttribute
    Boolean genStringAsCharArray
    String ieClassId
    String javaEncoding
    TrimSpaces trimSpaces
    Boolean xpoweredBy
    Boolean addWebXmlMappings
    FileCollection jspFiles
    Boolean failOnError
}
