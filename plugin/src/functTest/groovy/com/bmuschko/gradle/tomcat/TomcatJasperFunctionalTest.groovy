package com.bmuschko.gradle.tomcat

import com.bmuschko.gradle.tomcat.embedded.TomcatVersion
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Issue
import spock.lang.Unroll

class TomcatJasperFunctionalTest extends AbstractFunctionalTest {

    private static final String VALIDATE_XML_ATTRIBUTE = 'validateXml'
    private static final String VALIDATE_TLD_ATTRIBUTE = 'validateTld'

    def setup() {
        buildFile << """
            plugins {
                id 'com.bmuschko.tomcat'
            }
        """
        buildFile << basicBuildScript()
    }

    @Unroll
    def "Runs Jasper compiler for #tomcatVersion with default conventions"() {
        setup:
        File webAppDir = setupWebAppDirectory()
        createJspFiles(webAppDir)

        expect:
        File outputDir = temporaryFolder.newFolder('build', 'jasper')
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        build(TomcatPlugin.TOMCAT_JASPER_TASK_NAME)
        assertCompiledJsps(outputDir)

        where:
        tomcatVersion << [TomcatVersion.VERSION_6_0_X, TomcatVersion.VERSION_7_0_X, TomcatVersion.VERSION_8_0_X, TomcatVersion.VERSION_8_5_X, TomcatVersion.VERSION_9_0_X]
    }

    /**
     * With Tomcat version 7.0.50 the Jasper compiler Ant task validation attribute was changed from "validateXml"
     * to "validateTld". The end user of the Gradle task will need to select one of these attributes depending
     * on the Tomcat version in use.
     */
    @Unroll
    def "Can use Jasper compiler validation for Tomcat version #tomcatVersion with attribute #validationAttribute"() {
        setup:
        File webAppDir = setupWebAppDirectory()
        createJspFiles(webAppDir)

        expect:
        File outputDir = temporaryFolder.newFolder('build', 'jasper')
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << """
            tomcat {
                jasper {
                    $validationAttribute = true
                }
            }
        """
        build(TomcatPlugin.TOMCAT_JASPER_TASK_NAME)
        assertCompiledJsps(outputDir)

        where:
        tomcatVersion | validationAttribute
        '6.0.29'      | VALIDATE_XML_ATTRIBUTE
        '6.0.39'      | VALIDATE_TLD_ATTRIBUTE
        '7.0.42'      | VALIDATE_XML_ATTRIBUTE
        '7.0.50'      | VALIDATE_TLD_ATTRIBUTE
        '8.0.3'       | VALIDATE_TLD_ATTRIBUTE
        '9.0.1'       | VALIDATE_XML_ATTRIBUTE
    }

    @Unroll
    def "Throws exception using Jasper compiler validation for Tomcat version #tomcatVersion with invalid attribute #validationAttribute"() {
        setup:
        setupWebAppDirectory()

        when:
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        buildFile << """
            tomcat {
                jasper {
                    $validationAttribute = true
                }
            }
        """
        BuildResult result = buildAndFail(TomcatPlugin.TOMCAT_JASPER_TASK_NAME)

        then:
        result.output.contains("jasper doesn't support the \"$validationAttribute\" attribute")

        where:
        tomcatVersion | validationAttribute
        '6.0.39'      | VALIDATE_XML_ATTRIBUTE
        '7.0.42'      | VALIDATE_TLD_ATTRIBUTE
        '8.0.3'       | VALIDATE_XML_ATTRIBUTE
    }

    @Issue("https://github.com/bmuschko/gradle-tomcat-plugin/issues/162")
    def "Can use trim spaces option"() {
        setup:
        File webAppDir = setupWebAppDirectory()
        createJspFiles(webAppDir)

        expect:
        File outputDir = temporaryFolder.newFolder('build', 'jasper')
        buildFile << getBasicTomcatBuildFileContent(combinations[0])
        buildFile << """
            tomcat {
                jasper {
                    trimSpaces = com.bmuschko.gradle.tomcat.options.TrimSpaces.valueOf('${combinations[1]}')
                }
            }
        """
        build(TomcatPlugin.TOMCAT_JASPER_TASK_NAME)
        assertCompiledJsps(outputDir)

        where:
        combinations << [['6.0.39', '7.0.42', '7.0.50', '8.0.3', '9.0.1'], ['TRUE', 'FALSE', 'SINGLE']].combinations()
    }

    @Issue("https://github.com/bmuschko/gradle-tomcat-plugin/issues/158")
    def "Runs Jasper compiler twice to verify up to date checking"() {
        setup:
        File webAppDir = setupWebAppDirectory()
        createJspFiles(webAppDir)

        expect:
        File outputDir = temporaryFolder.newFolder('build', 'jasper')
        buildFile << getBasicTomcatBuildFileContent(tomcatVersion)
        assertTaskOutcome(build(TomcatPlugin.TOMCAT_JASPER_TASK_NAME), ':tomcatJasper', TaskOutcome.SUCCESS)
        assertCompiledJsps(outputDir)
        assertTaskOutcome(build(TomcatPlugin.TOMCAT_JASPER_TASK_NAME), ':tomcatJasper', TaskOutcome.UP_TO_DATE)


        where:
        tomcatVersion << [TomcatVersion.VERSION_6_0_X, TomcatVersion.VERSION_7_0_X, TomcatVersion.VERSION_8_0_X, TomcatVersion.VERSION_8_5_X, TomcatVersion.VERSION_9_0_X]
    }

    static void createJspFiles(File targetDir) {
        File helloWorldJspFile = new File(targetDir, 'helloWorld.jsp')
        helloWorldJspFile << """
            <html>
                <body>
                    <%= "Hello World!" %>
                </body>
            </html>
        """

        File dateJspFile = new File(targetDir, 'date.jsp')
        dateJspFile << """
            <%@ page language="java" import="java.util.*" errorPage="" %>
            <html>
                <body>
                    Current Date time: <%= new java.util.Date() %>
                </body>
            </html>
        """
    }

    static void assertCompiledJsps(File outputDir) {
        File compiledJspDir = new File(outputDir, 'org/apache/jsp')
        compiledJspDir.exists()
        assert new File(compiledJspDir, 'helloWorld_jsp.java').exists()
        assert new File(compiledJspDir, 'date_jsp.java').exists()
    }

    static void assertTaskOutcome(BuildResult result, String expectedTaskName, TaskOutcome expectedOutcome) {
        assert result.task(expectedTaskName).outcome.equals(expectedOutcome)
    }
}
