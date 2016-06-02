package com.bmuschko.gradle.tomcat

import com.bmuschko.gradle.tomcat.embedded.TomcatVersion
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.AvailablePortFinder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.junit.Assert.fail

abstract class AbstractFunctionalTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File projectDir
    File buildFile
    Integer httpPort
    Integer stopPort
    Integer ajpPort

    def setup() {
        projectDir = temporaryFolder.root
        buildFile = temporaryFolder.newFile('build.gradle')

        AvailablePortFinder availablePortFinder = AvailablePortFinder.createPrivate()
        httpPort = availablePortFinder.nextAvailable
        stopPort = availablePortFinder.nextAvailable
        ajpPort = availablePortFinder.nextAvailable
    }

    protected BuildResult build(String... arguments) {
        createAndConfigureGradleRunner(arguments).build()
    }

    protected BuildResult buildAndFail(String... arguments) {
        createAndConfigureGradleRunner(arguments).buildAndFail()
    }

    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        GradleRunner.create().withProjectDir(projectDir).withArguments(arguments).withPluginClasspath()
    }

    protected File createFile(File parent, String filename) {
        File file = new File(parent, filename)

        if(!file.createNewFile()) {
            fail("Unable to create file '${file.canonicalPath}'.")
        }

        file
    }

    protected File setupWebAppDirectory() {
        temporaryFolder.newFolder('src', 'main', 'webapp')
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

    protected String getBasicTomcat7xBuildFileContent(String version = '7.0.61') {
        """
            dependencies {
                def tomcatVersion = '$version'
                tomcat "org.apache.tomcat.embed:tomcat-embed-core:\${tomcatVersion}",
                       "org.apache.tomcat.embed:tomcat-embed-logging-juli:\${tomcatVersion}",
                       "org.apache.tomcat.embed:tomcat-embed-jasper:\${tomcatVersion}"
            }
        """
    }

    protected String getBasicTomcat8xBuildFileContent(String version = '8.0.21') {
        """
            dependencies {
                def tomcatVersion = '$version'
                tomcat "org.apache.tomcat.embed:tomcat-embed-core:\${tomcatVersion}",
                       "org.apache.tomcat.embed:tomcat-embed-logging-juli:\${tomcatVersion}",
                       "org.apache.tomcat.embed:tomcat-embed-jasper:\${tomcatVersion}"
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

    protected String basicBuildScript() {
        """
            apply plugin: 'java'

            repositories {
                mavenCentral()
            }
        """
    }
}