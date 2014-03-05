package org.gradle.api.plugins.tomcat

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.Task
import org.gradle.util.AvailablePortFinder
import spock.lang.Specification

import static org.spockframework.util.Assert.fail

abstract class AbstractIntegrationTest extends Specification {
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

        AvailablePortFinder availablePortFinder = AvailablePortFinder.createPrivate()
        Integer httpPort = availablePortFinder.nextAvailable
        Integer stopPort = availablePortFinder.nextAvailable

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

[tomcatRun, tomcatRunWar]*.daemon = true
[tomcatRun, tomcatRunWar]*.httpPort = $httpPort
[tomcatRun, tomcatRunWar, tomcatStop]*.stopKey = 'stopKey'
[tomcatRun, tomcatRunWar, tomcatStop]*.stopPort = $stopPort
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
}
