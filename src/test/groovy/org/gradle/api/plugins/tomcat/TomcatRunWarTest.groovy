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

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.plugins.tomcat.internal.Tomcat6xServer
import org.gradle.api.plugins.tomcat.internal.TomcatServer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.fail

/**
 * Test case for TomcatRunWar task.
 *
 * @author Benjamin Muschko
 */
class TomcatRunWarTest {
    private final File testDir = new File("build/tmp/tests")
    private Project project
    private TomcatRunWar tomcatRunWar

    @Before
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(testDir).build()
        tomcatRunWar = project.tasks.add(TomcatPlugin.TOMCAT_RUN_WAR, TomcatRunWar.class)
    }

    @After
    void tearDown() {
        tomcatRunWar = null

        if(testDir.exists()) {
            testDir.deleteDir()
        }
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForInvalidWebApp() {
        tomcatRunWar.validateConfiguration()
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForNonExistentWebApp() {
        tomcatRunWar.setWebApp new File(testDir, "webApp")
        tomcatRunWar.validateConfiguration()
    }

    @Test
    public void testValidateConfigurationForExistentWebApp() {
        File webAppDir = createWebAppDir()
        tomcatRunWar.setWebApp webAppDir
        tomcatRunWar.validateConfiguration()
        assert tomcatRunWar.getWebDefaultXml() == null
        assert tomcatRunWar.getConfigFile() == null
        assert tomcatRunWar.getWebApp() == webAppDir
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForNonExistentWebDefaultXml() {
        tomcatRunWar.setWebDefaultXml new File(testDir, "web.xml")
        tomcatRunWar.validateConfiguration()
    }

    @Test
    public void testValidateConfigurationForExistentWebDefaultXml() {
        File webAppDir = createWebAppDir()
        File webDefaultXml = createWebDefaultXml()
        tomcatRunWar.setWebApp webAppDir
        tomcatRunWar.setWebDefaultXml webDefaultXml
        tomcatRunWar.validateConfiguration()
        assert tomcatRunWar.getWebDefaultXml() == webDefaultXml
        assert tomcatRunWar.getConfigFile() == null
        assert tomcatRunWar.getWebApp() == webAppDir
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForNonExistentConfigFile() {
        tomcatRunWar.setConfigFile new File(testDir, "context.xml")
        tomcatRunWar.validateConfiguration()
    }

    @Test
    public void testValidateConfigurationForExistentConfigFile() {
        File webAppDir = createWebAppDir()
        File configFile = createConfigFile()
        tomcatRunWar.setWebApp webAppDir
        tomcatRunWar.setConfigFile configFile
        tomcatRunWar.validateConfiguration()
        assert tomcatRunWar.getWebDefaultXml() == null
        assert tomcatRunWar.getConfigFile() == configFile
        assert tomcatRunWar.getWebApp() == webAppDir
    }

    @Test
    public void testSetWebApplicationContextForFullContextPath() {
        File webAppDir = createWebAppDir()
        File configFile = createConfigFile()
        String contextPath = "/app"
        TomcatServer server = new Tomcat6xServer()
        tomcatRunWar.setServer server
        tomcatRunWar.setContextPath contextPath
        tomcatRunWar.setWebApp webAppDir
        tomcatRunWar.setConfigFile configFile
        tomcatRunWar.setWebApplicationContext()
        assert tomcatRunWar.getServer() == server
        assert tomcatRunWar.getServer().getContext().getDocBase() == webAppDir.getCanonicalPath()
        assert tomcatRunWar.getServer().getContext().getPath() == contextPath
        assert tomcatRunWar.getServer().getContext().getConfigFile() == configFile.getCanonicalPath()
    }

    @Test
    public void testSetWebApplicationContextForContextPathWithoutLeadingSlash() {
        File webAppDir = createWebAppDir()
        File configFile = createConfigFile()
        String contextPath = "app"
        TomcatServer server = new Tomcat6xServer()
        tomcatRunWar.setServer server
        tomcatRunWar.setContextPath contextPath
        tomcatRunWar.setWebApp webAppDir
        tomcatRunWar.setConfigFile configFile
        tomcatRunWar.setWebApplicationContext()
        assert tomcatRunWar.getServer() == server
        assert tomcatRunWar.getServer().getContext().getDocBase() == webAppDir.getCanonicalPath()
        assert tomcatRunWar.getServer().getContext().getPath() == "/" + contextPath
        assert tomcatRunWar.getServer().getContext().getConfigFile() == configFile.getCanonicalPath()
    }

    private File createWebAppDir() {
        File webAppDir = new File(testDir, "webApp")
        boolean success = webAppDir.mkdirs()

        if(!success) {
            fail "Unable to create web app directory"
        }

        webAppDir
    }

    private File createWebDefaultXml() {
        File webDefaultXml = new File(testDir, "web.xml")
        boolean success = webDefaultXml.createNewFile()

        if(!success) {
            fail "Unable to create web default XML"
        }

        webDefaultXml
    }

    private File createConfigFile() {
        File configFile = new File(testDir, "context.xml")
        boolean success = configFile.createNewFile()

        if(!success) {
            fail "Unable to create config file"
        }

        configFile
    }
}
