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
 * Test case for TomcatRun task.
 *
 * @author Benjamin Muschko
 */
class TomcatRunTest {
    private final File testDir = new File("build/tmp/tests")
    private Project project
    private TomcatRun tomcatRun

    @Before
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(testDir).build()
        tomcatRun = project.tasks.create(TomcatPlugin.TOMCAT_RUN_TASK_NAME, TomcatRun.class)
    }

    @After
    void tearDown() {
        tomcatRun = null

        if(testDir.exists()) {
            testDir.deleteDir()
        }
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForInvalidWebAppSourceDirectory() {
        tomcatRun.validateConfiguration()
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForNonExistentWebAppSourceDirectory() {
        tomcatRun.setWebAppSourceDirectory new File(testDir, "webapp")
        tomcatRun.validateConfiguration()
    }

    @Test
    public void testValidateConfigurationForExistentWebAppSourceDirectory() {
        File webAppSourceDir = createWebAppSourceDirectory()
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.validateConfiguration()
        assert tomcatRun.getWebDefaultXml() == null
        assert tomcatRun.getConfigFile() == null
        assert tomcatRun.getWebAppSourceDirectory() == webAppSourceDir
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForNonExistentWebDefaultXml() {
        tomcatRun.setWebDefaultXml new File(testDir, "web.xml")
        tomcatRun.validateConfiguration()
    }

    @Test
    public void testValidateConfigurationForExistentWebDefaultXml() {
        File webAppSourceDir = createWebAppSourceDirectory()
        File webDefaultXml = createWebDefaultXml()
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setWebDefaultXml webDefaultXml
        tomcatRun.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.validateConfiguration()
        assert tomcatRun.getWebDefaultXml() == webDefaultXml
        assert tomcatRun.getConfigFile() == null
        assert tomcatRun.getWebAppSourceDirectory() == webAppSourceDir
    }

    @Test(expected = InvalidUserDataException.class)
    public void testValidateConfigurationForNonExistentConfigFile() {
        tomcatRun.setConfigFile new File(testDir, "context.xml")
        tomcatRun.validateConfiguration()
    }

    @Test
    public void testValidateConfigurationForExistentConfigFile() {
        File webAppSourceDir = createWebAppSourceDirectory()
        File configFile = createConfigFile()
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setConfigFile configFile
        tomcatRun.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.validateConfiguration()
        assert tomcatRun.getWebDefaultXml() == null
        assert tomcatRun.getConfigFile() == configFile
        assert tomcatRun.getWebAppSourceDirectory() == webAppSourceDir
    }

    @Test
    public void testValidateConfigurationForExistentDefaultConfigFile() {
        File webAppSourceDir = createWebAppSourceDirectory()
        File defaultConfigFile = createDefaultConfigFile(webAppSourceDir)
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.validateConfiguration()
        assert tomcatRun.getWebDefaultXml() == null
        assert tomcatRun.getResolvedConfigFile().path == defaultConfigFile.toURI().toURL().path
        assert tomcatRun.getWebAppSourceDirectory() == webAppSourceDir
    }

    @Test
    void testValidateConfigurationForEnabledSSLButNoKeystoreConfigured() {
        File webAppSourceDir = createWebAppSourceDirectory()
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.enableSSL = true
        tomcatRun.validateConfiguration()
        assert tomcatRun.getEnableSSL() == true
        assert tomcatRun.getKeystoreFile() == null
        assert tomcatRun.getKeystorePass() == null
    }

    @Test(expected = InvalidUserDataException.class)
    void testValidateConfigurationForEnabledSSLButOnlyKeystoreFileConfigured() {
        File webAppSourceDir = createWebAppSourceDirectory()
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setKeystoreFile createKeystoreFile()
        tomcatRun.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.enableSSL = true
        tomcatRun.validateConfiguration()
    }

    @Test(expected = InvalidUserDataException.class)
    void testValidateConfigurationForEnabledSSLButOnlyKeystorePasswordConfigured() {
        File webAppSourceDir = createWebAppSourceDirectory()
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setKeystorePass 'pwd'
        tomcatRun.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.enableSSL = true
        tomcatRun.validateConfiguration()
    }

    @Test
    void testValidateConfigurationForEnabledSSLForKeystoreFileAndPasswordConfigured() {
        File webAppSourceDir = createWebAppSourceDirectory()
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setKeystoreFile createKeystoreFile()
        tomcatRun.setKeystorePass 'pwd'
        tomcatRun.setHttpProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.setHttpsProtocol TomcatPluginConvention.DEFAULT_PROTOCOL_HANDLER
        tomcatRun.enableSSL = true
        tomcatRun.validateConfiguration()
        assert tomcatRun.getEnableSSL() == true
        assert tomcatRun.getKeystoreFile() == new File(testDir, "keystore.jks")
        assert tomcatRun.getKeystorePass() == 'pwd'
    }

    @Test
    public void testSetWebApplicationContextForFullContextPath() {
        File webAppSourceDir = createWebAppSourceDirectory()
        String contextPath = "/app"
        TomcatServer server = new Tomcat6xServer()
        tomcatRun.setServer server
        tomcatRun.setContextPath contextPath
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setWebApplicationContext()
        assert tomcatRun.getServer() == server
        assert tomcatRun.getServer().getContext().getDocBase() == webAppSourceDir.getCanonicalPath()
        assert tomcatRun.getServer().getContext().getPath() == contextPath
    }

    @Test
    public void testSetWebApplicationContextForContextPathWithoutLeadingSlash() {
        File webAppSourceDir = createWebAppSourceDirectory()
        String contextPath = "app"
        TomcatServer server = new Tomcat6xServer()
        tomcatRun.setServer server
        tomcatRun.setContextPath contextPath
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setWebApplicationContext()
        assert tomcatRun.getServer() == server
        assert tomcatRun.getServer().getContext().getDocBase() == webAppSourceDir.getCanonicalPath()
        assert tomcatRun.getServer().getContext().getPath() == "/" + contextPath
    }

    @Test
    public void testConfigureWebApplication() {
        File webAppSourceDir = createWebAppSourceDirectory()
        String contextPath = "app"
        TomcatServer server = new Tomcat6xServer()
        tomcatRun.setServer server
        tomcatRun.setContextPath contextPath
        tomcatRun.setWebAppSourceDirectory webAppSourceDir
        tomcatRun.setWebAppClasspath project.files("jars")
        tomcatRun.reloadable = true
        tomcatRun.configureWebApplication()
        assert tomcatRun.getServer() == server
        assert tomcatRun.getServer().getContext().getDocBase() == webAppSourceDir.getCanonicalPath()
        assert tomcatRun.getServer().getContext().getPath() == "/" + contextPath
        assert tomcatRun.getServer().getContext().getReloadable() == true
        assert tomcatRun.getServer().getContext().getLoader().getRepositories().size() == 1
        assert tomcatRun.getServer().getContext().getLoader().getRepositories()[0] == new File(testDir, "jars").toURI().toURL().toString()
    }

    @Test
    public void testSetWebApplicationContextForRootContextUrl() {
        String contextPath = '/'
        tomcatRun.setContextPath contextPath
        assert tomcatRun.getFullContextPath() == ''
    }

    @Test
    public void testSetWebApplicationContextForBlankContextUrl() {
        String contextPath = ''
        tomcatRun.setContextPath contextPath
        assert tomcatRun.getFullContextPath() == ''
    }

    @Test
    public void testSetWebApplicationContextForContextUrlWithLeadingSlash() {
        String contextPath = '/app'
        tomcatRun.setContextPath contextPath
        assert tomcatRun.getFullContextPath() == '/app'
    }

    @Test
    public void testSetWebApplicationContextForContextUrlWithoutLeadingSlash() {
        String contextPath = 'app'
        tomcatRun.setContextPath contextPath
        assert tomcatRun.getFullContextPath() == '/app'
    }

    private File createWebAppSourceDirectory() {
        File webAppSourceDir = new File(testDir, "webapp")
        boolean success = webAppSourceDir.mkdirs()

        if(!success) {
            fail "Unable to create web app source directory"
        }

        webAppSourceDir
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

    private File createDefaultConfigFile(File webAppSourceDir) {
        File metaInfDir = new File(webAppSourceDir, "META-INF")
        metaInfDir.mkdir()
        File defaultConfigFile = new File(metaInfDir, "context.xml")
        boolean success = defaultConfigFile.createNewFile()

        if(!success) {
            fail "Unable to create default config file"
        }

        defaultConfigFile
    }

    private File createKeystoreFile() {
        File keystoreFile = new File(testDir, "keystore.jks")
        boolean success = keystoreFile.createNewFile()

        if(!success) {
            fail "Unable to create keystore file"
        }

        keystoreFile
    }
}
