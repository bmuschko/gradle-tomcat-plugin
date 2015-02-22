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
package com.bmuschko.gradle.tomcat

import com.bmuschko.gradle.tomcat.embedded.TomcatVersion
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.Task
import org.gradle.util.AvailablePortFinder
import spock.lang.Specification

import static org.spockframework.util.Assert.fail

abstract class AbstractIntegrationTest extends Specification {
    File integTestDir = new File('build/integTest')
    File buildFile
    Integer httpPort
    Integer stopPort
    Integer ajpPort

    def setup() {
        deleteDir(integTestDir)
        createDir(integTestDir)
        buildFile = createFile(integTestDir, 'build.gradle')

        AvailablePortFinder availablePortFinder = AvailablePortFinder.createPrivate()
        httpPort = availablePortFinder.nextAvailable
        stopPort = availablePortFinder.nextAvailable
        ajpPort = availablePortFinder.nextAvailable

        buildFile << """
buildscript {
    dependencies {
        classpath files('../../../embedded/build/classes/main',
                        '../classes/main',
                        '../../../tomcat6x/build/classes/main',
                        '../../../tomcat7x/build/classes/main',
                        '../../../tomcat8x/build/classes/main')
    }
}

apply plugin: 'java'

repositories {
    mavenCentral()
}
"""
    }

    def cleanup() {
        deleteDir(integTestDir)
    }

    protected void deleteDir(File dir) {
         if(dir.exists()) {
             if(!integTestDir.deleteDir()) {
                 fail("Unable to delete directory '$dir.canonicalPath'.")
             }
         }
    }

    protected void createDir(File dir) {
        if(!dir.exists()) {
            if(!dir.mkdirs()) {
                fail("Unable to create directory '$dir.canonicalPath'.")
            }
        }
    }

    protected File createFile(File parent, String filename) {
        File file = new File(parent, filename)

        if(!file.createNewFile()) {
            fail("Unable to create file '${file.canonicalPath}'.")
        }

        file
    }

    protected Task findTask(GradleProject project, String name) {
        project.tasks.find { task -> task.name == name }
    }

    protected void setupWebAppDirectory() {
        File webappDir = new File(integTestDir, 'src/main/webapp')

        if(!webappDir.mkdirs()) {
            fail('Unable to create web application source directory.')
        }
    }

    protected GradleInvocationResult runTasks(File projectDir, String... tasks) {
        GradleConnector gradleConnector = GradleConnector.newConnector()
        gradleConnector.forProjectDirectory(projectDir)
        ProjectConnection connection = gradleConnector.connect()

        try {
            BuildLauncher builder = connection.newBuild()
            OutputStream outputStream = new ByteArrayOutputStream()
            builder.setStandardOutput(outputStream)
            builder.forTasks(tasks).run()
            GradleProject gradleProject = connection.getModel(GradleProject)
            return new GradleInvocationResult(project: gradleProject, output: new String(outputStream.toByteArray(), 'UTF-8'))
        }
        finally {
            connection?.close()
        }
    }

    protected String getBasicTomcatBuildFileContent(TomcatVersion tomcatVersion) {
        switch(tomcatVersion) {
            case TomcatVersion.VERSION_6X: return getBasicTomcat6xBuildFileContent()
            case TomcatVersion.VERSION_7X: return getBasicTomcat7xBuildFileContent()
            case TomcatVersion.VERSION_8X: return getBasicTomcat8xBuildFileContent()
            default: throw new IllegalArgumentException("Unknown Tomcat version $tomcatVersion")
        }
    }

    protected String getBasicTomcatBuildFileContent(String tomcatVersion) {
        TomcatVersion identifiedTomcatVersion = TomcatVersion.getTomcatVersionForString(tomcatVersion)

        switch(identifiedTomcatVersion) {
            case TomcatVersion.VERSION_6X: return getBasicTomcat6xBuildFileContent(tomcatVersion)
            case TomcatVersion.VERSION_7X: return getBasicTomcat7xBuildFileContent(tomcatVersion)
            case TomcatVersion.VERSION_8X: return getBasicTomcat8xBuildFileContent(tomcatVersion)
            default: throw new IllegalArgumentException("Unknown Tomcat version $tomcatVersion")
        }
    }

    protected String getBasicTomcat6xBuildFileContent(String version = '6.0.43') {
        """
dependencies {
    def tomcatVersion = '$version'
    tomcat "org.apache.tomcat:catalina:\${tomcatVersion}",
           "org.apache.tomcat:coyote:\${tomcatVersion}",
           "org.apache.tomcat:jasper:\${tomcatVersion}"
}
"""
    }

    protected String getBasicTomcat7xBuildFileContent(String version = '7.0.59') {
        """
dependencies {
    def tomcatVersion = '$version'
    tomcat "org.apache.tomcat.embed:tomcat-embed-core:\${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-logging-juli:\${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-jasper:\${tomcatVersion}"
}
"""
    }

    protected String getBasicTomcat8xBuildFileContent(String version = '8.0.18') {
        """
dependencies {
    def tomcatVersion = '$version'
    tomcat "org.apache.tomcat.embed:tomcat-embed-core:\${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-logging-juli:\${tomcatVersion}"
    tomcat("org.apache.tomcat.embed:tomcat-embed-jasper:\${tomcatVersion}") {
        exclude group: 'org.eclipse.jdt.core.compiler', module: 'ecj'
    }
}
"""
    }

    protected String getTomcatContainerLifecycleManagementBuildFileContent(String tomcatStartTask, String tomcatStopTask) {
        """
task startAndStopTomcat {
    dependsOn $tomcatStartTask
    finalizedBy $tomcatStopTask
}
"""
    }
}
