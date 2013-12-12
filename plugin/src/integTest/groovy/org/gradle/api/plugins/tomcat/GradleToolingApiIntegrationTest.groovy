package org.gradle.api.plugins.tomcat

import org.gradle.api.plugins.tomcat.embedded.PortFinder
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject
import spock.lang.Specification

import static org.spockframework.util.Assert.fail

abstract class GradleToolingApiIntegrationTest extends Specification {
    File integTestDir
    File buildFile

    def setup() {
        integTestDir = new File('build/integTest')

        if(!integTestDir.mkdirs()) {
            fail('Unable to create integration test directory.')
        }

        buildFile = new File(integTestDir, 'build.gradle')

        if(!buildFile.createNewFile()) {
            fail('Unable to create Gradle build script.')
        }

        setupWebAppDirectories()
        Integer httpPort = 8080
        Integer stopPort = 8081

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
apply plugin: org.gradle.api.plugins.tomcat.TomcatPlugin

repositories {
    mavenCentral()
}

tomcatRun.daemon = true
tomcatRun.httpPort = $httpPort
[tomcatRun, tomcatStop]*.stopKey = 'stopKey'
[tomcatRun, tomcatStop]*.stopPort = $stopPort

task startAndStopTomcat {
    dependsOn tomcatRun
    finalizedBy tomcatStop
}
"""
    }

    def cleanup() {
        if(!buildFile.delete()) {
            fail('Unable to delete Gradle build script.')
        }

        if(!integTestDir.deleteDir()) {
            fail('Unable to delete integration test directory.')
        }
    }

    private void setupWebAppDirectories() {
        File webappDir = new File(integTestDir, 'src/main/webapp')

        if(!webappDir.mkdirs()) {
            fail('Unable to create web application source directory.')
        }

        File classesDir = new File(integTestDir, 'build/classes/main')

        if(!classesDir.mkdirs()) {
            fail('Unable to create classes directory.')
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
}
