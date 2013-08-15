/*
 * Copyright 2011 the original author or authors.
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

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.plugins.tomcat.embedded.Tomcat6xServer
import org.gradle.api.plugins.tomcat.embedded.TomcatServer
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
        tomcatRunWar = project.tasks.create(TomcatPlugin.TOMCAT_RUN_WAR_TASK_NAME, TomcatRunWar.class)
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
        tomcatRunWar.setWebApp new File(testDir, "webApp.war")
        tomcatRunWar.validateConfiguration()
    }

    @Test
    public void testValidateConfigurationForExistentWebApp() {
        File webAppDir = createWebAppDir()
        File war = createWar(webAppDir)
        tomcatRunWar.setWebApp war
        tomcatRunWar.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRunWar.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRunWar.validateConfiguration()
        assert tomcatRunWar.getWebDefaultXml() == null
        assert tomcatRunWar.getConfigFile() == null
        assert tomcatRunWar.getWebApp() == war
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForNonExistentWebDefaultXml() {
        tomcatRunWar.setWebDefaultXml new File(testDir, "web.xml")
        tomcatRunWar.validateConfiguration()
    }

    @Test
    public void testValidateConfigurationForExistentWebDefaultXml() {
        File webAppDir = createWebAppDir()
        File war = createWar(webAppDir)
        File webDefaultXml = createWebDefaultXml()
        tomcatRunWar.setWebApp war
        tomcatRunWar.setWebDefaultXml webDefaultXml
        tomcatRunWar.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRunWar.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRunWar.validateConfiguration()
        assert tomcatRunWar.getWebDefaultXml() == webDefaultXml
        assert tomcatRunWar.getConfigFile() == null
        assert tomcatRunWar.getWebApp() == war
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForNonExistentConfigFile() {
        tomcatRunWar.setConfigFile new File(testDir, "context.xml")
        tomcatRunWar.validateConfiguration()
    }

    @Test
    public void testValidateConfigurationForExistentConfigFile() {
        File webAppDir = createWebAppDir()
        File war = createWar(webAppDir)
        File configFile = createConfigFile()
        tomcatRunWar.setWebApp war
        tomcatRunWar.setConfigFile configFile
        tomcatRunWar.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRunWar.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRunWar.validateConfiguration()
        assert tomcatRunWar.getWebDefaultXml() == null
        assert tomcatRunWar.getConfigFile() == configFile
        assert tomcatRunWar.getWebApp() == war
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
    }

    @Test
    public void testSetWebApplicationContextForRootContextUrl() {
        String contextPath = '/'
        tomcatRunWar.setContextPath contextPath
        assert tomcatRunWar.getFullContextPath() == ''
    }

    @Test
    public void testSetWebApplicationContextForBlankContextUrl() {
        String contextPath = ''
        tomcatRunWar.setContextPath contextPath
        assert tomcatRunWar.getFullContextPath() == ''
    }

    @Test
    public void testSetWebApplicationContextForContextUrlWithLeadingSlash() {
        String contextPath = '/app'
        tomcatRunWar.setContextPath contextPath
        assert tomcatRunWar.getFullContextPath() == '/app'
    }

    @Test
    public void testSetWebApplicationContextForContextUrlWithoutLeadingSlash() {
        String contextPath = 'app'
        tomcatRunWar.setContextPath contextPath
        assert tomcatRunWar.getFullContextPath() == '/app'
    }

    private File createWebAppDir() {
        File webAppDir = new File(testDir, "webApp")
        boolean success = webAppDir.mkdirs()

        if(!success) {
            fail "Unable to create web app directory"
        }

        webAppDir
    }

    private File createWar(File webAppDir) {
        File war = new File(webAppDir, "test.war")
        File zippedFile = new File(webAppDir, "entry.txt")
        boolean success = zippedFile.createNewFile()

        if(!success) {
            fail "Unable to create test file for WAR"
        }

        ZipOutputStream out = null

        try {
            out = new ZipOutputStream(new FileOutputStream(war))
            out.putNextEntry(new ZipEntry(zippedFile.canonicalPath))
        }
        catch(IOException e) {
            fail "Unable to create WAR"
        }
        finally {
            if(out) {
                out.close()
            }
        }

        war
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
