package org.gradle.api.plugins.tomcat

import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.Task

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
}
