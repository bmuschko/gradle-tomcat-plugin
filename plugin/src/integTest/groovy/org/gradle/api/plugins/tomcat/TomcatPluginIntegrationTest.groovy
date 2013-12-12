package org.gradle.api.plugins.tomcat

import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.Task
import spock.lang.Ignore

class TomcatPluginIntegrationTest extends GradleToolingApiIntegrationTest {
    def "Adds default Tomcat tasks for Java project"() {
        when:
            GradleProject project = runTasks(integTestDir, 'tasks')
        then:
            Task tomcatRunTask = project.tasks.find { task -> task.name == TomcatPlugin.TOMCAT_RUN_TASK_NAME }
            tomcatRunTask
            tomcatRunTask.description == 'Uses your files as and where they are and deploys them to Tomcat.'
            Task tomcatRunWarTask = project.tasks.find { task -> task.name == TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME }
            tomcatRunWarTask
            tomcatRunWarTask.description == 'Assembles the webapp into a war and deploys it to Tomcat.'
            Task tomcatStopTask = project.tasks.find { task -> task.name == TomcatPlugin.TOMCAT_STOP_TASK_NAME }
            tomcatStopTask
            tomcatStopTask.description == 'Stops Tomcat.'
            Task tomcatJasperTask = project.tasks.find { task -> task.name == TomcatPlugin.TOMCAT_JASPER_TASK_NAME }
            tomcatJasperTask
            tomcatJasperTask.description == 'Runs the JSP compiler and turns JSP pages into Java source.'
    }

    def "Start and stop Tomcat 6x"() {
        expect:
            buildFile << """
dependencies {
    def tomcatVersion = '6.0.29'
    tomcat "org.apache.tomcat:catalina:\${tomcatVersion}",
           "org.apache.tomcat:coyote:\${tomcatVersion}",
           "org.apache.tomcat:jasper:\${tomcatVersion}"
}
"""
            runTasks(integTestDir, 'startAndStopTomcat')
    }

    def "Start and stop Tomcat 7x"() {
        expect:
            buildFile << """
dependencies {
    def tomcatVersion = '7.0.11'
    tomcat "org.apache.tomcat.embed:tomcat-embed-core:\${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-logging-juli:\${tomcatVersion}"
    tomcat("org.apache.tomcat.embed:tomcat-embed-jasper:\${tomcatVersion}") {
        exclude group: 'org.eclipse.jdt.core.compiler', module: 'ecj'
    }
}
"""
            runTasks(integTestDir, 'startAndStopTomcat')
    }

    @Ignore
    def "Start and stop Tomcat 8x"() {
        expect:
            buildFile << """
dependencies {
    def tomcatVersion = '8.0.0-RC5'
    tomcat "org.apache.tomcat.embed:tomcat-embed-core:\${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-logging-juli:\${tomcatVersion}"
    tomcat("org.apache.tomcat.embed:tomcat-embed-jasper:\${tomcatVersion}") {
        exclude group: 'org.eclipse.jdt.core.compiler', module: 'ecj'
    }
}
"""
            runTasks(integTestDir, 'startAndStopTomcat')
    }
}
