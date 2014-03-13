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

import org.gradle.api.plugins.tomcat.embedded.TomcatVersion
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

    def setup() {
        if(!integTestDir.exists() && !integTestDir.mkdirs()) {
            fail('Unable to create integration test directory.')
        }

        buildFile = new File(integTestDir, 'build.gradle')

        if(!buildFile.createNewFile()) {
            fail('Unable to create Gradle build script.')
        }

        AvailablePortFinder availablePortFinder = AvailablePortFinder.createPrivate()
        httpPort = availablePortFinder.nextAvailable
        stopPort = availablePortFinder.nextAvailable

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
        if(!integTestDir.deleteDir()) {
            fail('Unable to delete integration test directory.')
        }
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

    protected GradleProject runTasks(File projectDir, String... tasks) {
        GradleConnector gradleConnector = GradleConnector.newConnector()
        gradleConnector.forProjectDirectory(projectDir)
        ProjectConnection connection = gradleConnector.connect()

        try {
            BuildLauncher builder = connection.newBuild()
            builder.forTasks(tasks).run()
            return connection.getModel(GradleProject)
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

    private String getBasicTomcat6xBuildFileContent() {
        """
dependencies {
    def tomcatVersion = '6.0.29'
    tomcat "org.apache.tomcat:catalina:\${tomcatVersion}",
           "org.apache.tomcat:coyote:\${tomcatVersion}",
           "org.apache.tomcat:jasper:\${tomcatVersion}"
}
"""
    }

    private String getBasicTomcat7xBuildFileContent() {
        """
dependencies {
    def tomcatVersion = '7.0.11'
    tomcat "org.apache.tomcat.embed:tomcat-embed-core:\${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-logging-juli:\${tomcatVersion}"
    tomcat("org.apache.tomcat.embed:tomcat-embed-jasper:\${tomcatVersion}") {
        exclude group: 'org.eclipse.jdt.core.compiler', module: 'ecj'
    }
}
"""
    }

    private String getBasicTomcat8xBuildFileContent() {
        """
dependencies {
    def tomcatVersion = '8.0.3'
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
