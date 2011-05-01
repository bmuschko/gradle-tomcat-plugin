/*
 * Copyright 2010 the original author or authors.
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

import org.gradle.api.Project
import org.gradle.api.plugins.WarPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.Task
import org.gradle.api.plugins.WarPluginConvention

/**
 * Test case for Tomcat plugin.
 *
 * @author Benjamin Muschko
 */
class TomcatPluginTest {
    private final File testDir = new File("build/tmp/tests")
    private Project project
    private TomcatPlugin tomcatPlugin

    @Before
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(testDir).build()
        tomcatPlugin = new TomcatPlugin().apply(project)
    }

    @After
    void tearDown() {
        tomcatPlugin = null

        if(testDir.exists()) {
            testDir.deleteDir()
        }
    }

    @Test
    public void testApplyBasicSetup() {
        assert project.plugins.hasPlugin(WarPlugin) == true
        assert project.convention.plugins.tomcat instanceof TomcatPluginConvention == true
    }

    @Test
    public void testApplyTomcatRunTask() {
        Task task = project.tasks[TomcatPlugin.TOMCAT_RUN]
        assert task instanceof TomcatRun == true
        assert task.description == "Uses your files as and where they are and deploys them to Tomcat."
        assert task.group == WarPlugin.WEB_APP_GROUP
        assert task.contextPath == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName
        assert task.httpPort == project.httpPort
        assert task.stopPort == project.stopPort
        assert task.stopKey == project.stopKey
        assert task.daemon == false
        assert task.reloadable == true
        assert task.classpath == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).classpath
        assert task.webAppSourceDirectory == project.convention.getPlugin(WarPluginConvention.class).webAppDir
    }

    @Test
    public void testApplyTomcatRunWarTask() {
        Task task = project.tasks[TomcatPlugin.TOMCAT_RUN_WAR]
        assert task instanceof TomcatRunWar == true
        assert task.description == "Assembles the webapp into a war and deploys it to Tomcat."
        assert task.group == WarPlugin.WEB_APP_GROUP
        assert task.contextPath == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).baseName
        assert task.httpPort == project.httpPort
        assert task.stopPort == project.stopPort
        assert task.stopKey == project.stopKey
        assert task.daemon == false
        assert task.reloadable == true
        assert task.webApp == project.tasks.getByName(WarPlugin.WAR_TASK_NAME).archivePath
    }

    @Test
    public void testApplyTomcatStopTask() {
        Task task = project.tasks[TomcatPlugin.TOMCAT_STOP]
        assert task instanceof TomcatStop == true
        assert task.description == "Stops Tomcat."
        assert task.group == WarPlugin.WEB_APP_GROUP
        assert task.stopPort == project.stopPort
        assert task.stopKey == project.stopKey
    }
}
