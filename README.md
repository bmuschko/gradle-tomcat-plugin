# Gradle Tomcat plugin [![Build Status](https://github.com/bmuschko/gradle-tomcat-plugin/workflows/Build%20and%20Release%20%5BLinux%5D/badge.svg)](https://github.com/bmuschko/gradle-tomcat-plugin/actions?query=workflow%3A%22Build+and+Release+%5BLinux%5D%22)

![Tomcat Logo](http://tomcat.apache.org/res/images/tomcat.png)

<table border=1>
    <tr>
        <td>
            Over the past couple of years this plugin has seen many releases. Thanks to everyone involved! 
            Unfortunately, I don't have much time to contribute anymore. In practice this means far less activity, 
            responsiveness on issues and new releases from my end.
        </td>
    </tr>
    <tr>
        <td>
            I am 
            <a href="https://discuss.gradle.org/t/looking-for-new-owners-for-gradle-plugins/9735">actively looking for contributors</a> 
            willing to take on maintenance and implementation of the project. If you are interested and would love to see this 
            plugin continue to thrive, shoot me a <a href="mailto:benjamin.muschko@gmail.com">mail</a>.
        </td>
    </tr>
</table>

The plugin provides deployment capabilities of web applications to an embedded Tomcat web container in any given
Gradle build. It extends the [War plugin](https://docs.gradle.org/current/userguide/war_plugin.html). At the moment the Tomcat versions
6.0.x, 7.0.x, 8.0.x, 8.5.x and 9.0.x are supported.

The typical use case for this plugin is to support deployment during development. The plugin allows for rapid web application
development due to the container's fast startup times. Gradle starts the embedded container in the same JVM. Currently,
the container cannot be forked as a separate process. This plugin also can't deploy a WAR file to a remote container. If
you are looking for this capability, please have a look at the [Cargo plugin](https://github.com/bmuschko/gradle-cargo-plugin)
instead.

## Usage

To use the plugin's functionality, you will need to add the its binary artifact to your build script's classpath and apply the plugin.

### Adding the plugin binary to the build

The plugin JAR needs to be defined in the classpath of your build script. It is directly available on
[Bintray](https://bintray.com/bmuschko/gradle-plugins/com.bmuschko%3Agradle-tomcat-plugin).
The following code snippet shows an example on how to retrieve it from Maven Central:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'com.bmuschko:gradle-tomcat-plugin:2.5'
    }
}
```

### Provided plugins

The JAR file comes with two plugins:

<table>
    <tr>
        <th>Plugin Identifier</th>
        <th>Depends On</th>
        <th>Type</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>com.bmuschko.tomcat-base</td>
        <td>-</td>
        <td><a href="http://bmuschko.github.io/gradle-tomcat-plugin/docs/groovydoc/com/bmuschko/gradle/tomcat/TomcatBasePlugin.html">TomcatBasePlugin</a></td>
        <td>Provides Tomcat custom task types, pre-configures classpath.</td>
    </tr>
    <tr>
        <td>com.bmuschko.tomcat</td>
        <td>com.bmuschko.tomcat-base</td>
        <td><a href="http://bmuschko.github.io/gradle-tomcat-plugin/docs/groovydoc/com/bmuschko/gradle/tomcat/TomcatPlugin.html">TomcatPlugin</a></td>
        <td>Provides tasks for starting and stopping an embedded Tomcat container and exposes extension named <code>tomcat</code>.</td>
    </tr>
</table>

The `com.bmuschko.tomcat` plugin helps you get started quickly. If you are OK if the preconfigured tasks, this is the
preferrable option. Most plugin users will go with this option. To use the Tomcat plugin, include the following code snippet
in your build script:

    apply plugin: 'com.bmuschko.tomcat'

If you need full control over your tasks or don't want to go with the preconfigured tasks, you will want to use the `com.bmuschko.tomcat-base`
plugin. That might be the case if you want to set up the container solely for functional testing. The downside is that each task
has to be configured individually in your build script. To use the Tomcat base plugin, include the following code snippet
in your build script:

    apply plugin: 'com.bmuschko.tomcat-base'

### Assigning the Tomcat libraries

Additionally, the Tomcat runtime libraries need to be added to the configuration `tomcat`. At the moment the Tomcat
versions 6.0.x, 7.0.x, 8.0.x, 8.5.x and 9.0.x are supported by the plugin. Make sure you don't mix up Tomcat libraries of different
versions.

**Tomcat 6.0.x:**

```groovy
repositories {
    mavenCentral()
}

dependencies {
    def tomcatVersion = '6.0.51'
    tomcat "org.apache.tomcat:catalina:${tomcatVersion}",
           "org.apache.tomcat:coyote:${tomcatVersion}",
           "org.apache.tomcat:jasper:${tomcatVersion}"
}
```

**Tomcat 7.0.x:**

```groovy
repositories {
    mavenCentral()
}

dependencies {
    def tomcatVersion = '7.0.76'
    tomcat "org.apache.tomcat.embed:tomcat-embed-core:${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-logging-juli:${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-jasper:${tomcatVersion}"
}
```

**Tomcat 8.0.x:**

```groovy
repositories {
    mavenCentral()
}

dependencies {
    def tomcatVersion = '8.0.42'
    tomcat "org.apache.tomcat.embed:tomcat-embed-core:${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-logging-juli:${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-jasper:${tomcatVersion}"
}
```

**Tomcat 8.5.x:**

Please be aware that the dependency `tomcat-embed-logging-juli` is only required to enable container logging via Log4J 1.x (which is no longer support by the Log4J community). Log4J 2.x can be used for container logging without declaring any extra libraries.

```groovy
repositories {
    mavenCentral()
}

dependencies {
    def tomcatVersion = '8.5.16'
    tomcat "org.apache.tomcat.embed:tomcat-embed-core:${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-logging-juli:8.5.2",
           "org.apache.tomcat.embed:tomcat-embed-jasper:${tomcatVersion}"
}

tomcat {
    httpProtocol = 'org.apache.coyote.http11.Http11Nio2Protocol'
    ajpProtocol  = 'org.apache.coyote.ajp.AjpNio2Protocol'
}
```

**Tomcat 9.0.x:**

Please be aware that the dependency `tomcat-embed-logging-juli` is only required to enable container logging via Log4J 1.x (which is no longer support by the Log4J community). Log4J 2.x can be used for container logging without declaring any extra libraries.

```groovy
repositories {
    mavenCentral()
}

dependencies {
    def tomcatVersion = '9.0.1'
    tomcat "org.apache.tomcat.embed:tomcat-embed-core:${tomcatVersion}",
           "org.apache.tomcat.embed:tomcat-embed-logging-juli:9.0.0.M6",
           "org.apache.tomcat.embed:tomcat-embed-jasper:${tomcatVersion}"
}

tomcat {
    httpProtocol = 'org.apache.coyote.http11.Http11Nio2Protocol'
    ajpProtocol  = 'org.apache.coyote.ajp.AjpNio2Protocol'
}
```

## Tasks

The `com.bmuschko.tomcat` plugin pre-defines the following tasks out-of-the-box:

<table>
    <tr>
        <th>Task Name</th>
        <th>Depends On</th>
        <th>Type</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>tomcatRun</td>
        <td>-</td>
        <td><a href="http://bmuschko.github.io/gradle-tomcat-plugin/docs/groovydoc/com/bmuschko/gradle/tomcat/tasks/TomcatRun.html">TomcatRun</a></td>
        <td>Starts a Tomcat instance and deploys the exploded web application to it.</td>
    </tr>
    <tr>
        <td>tomcatRunWar</td>
        <td>-</td>
        <td><a href="http://bmuschko.github.io/gradle-tomcat-plugin/docs/groovydoc/com/bmuschko/gradle/tomcat/tasks/TomcatRunWar.html">TomcatRunWar</a></td>
        <td>Starts a Tomcat instance and deploys the WAR to it.</td>
    </tr>
    <tr>
        <td>tomcatStop</td>
        <td>-</td>
        <td><a href="http://bmuschko.github.io/gradle-tomcat-plugin/docs/groovydoc/com/bmuschko/gradle/tomcat/tasks/TomcatStop.html">TomcatStop</a></td>
        <td>Stops the Tomcat instance.</td>
    </tr>
    <tr>
        <td>tomcatJasper</td>
        <td>-</td>
        <td><a href="http://bmuschko.github.io/gradle-tomcat-plugin/docs/groovydoc/com/bmuschko/gradle/tomcat/tasks/TomcatJasper.html">TomcatJasper</a></td>
        <td>Runs the JSP compiler and turns JSP pages into Java source using <a href="http://tomcat.apache.org/tomcat-7.0-doc/jasper-howto.html">Jasper</a>.</td>
    </tr>
</table>

## Project layout

The Tomcat plugin uses the same layout as the War plugin.

## Extension properties

The Tomcat plugin exposes the following properties through the extension named `tomcat`:

* `httpPort`: The TCP port which Tomcat should listen for HTTP requests on (defaults to `8080`).
* `httpsPort`: The TCP port which Tomcat should listen for HTTPS requests on (defaults to `8443`).
* `ajpPort`: The TCP port which Tomcat should listen for AJP requests on (defaults to `8009`).
* `stopPort`: The TCP port which Tomcat should listen for admin requests on (defaults to `8081`).
* `stopKey`: The key to pass to Tomcat when requesting it to stop (defaults to `null`).
* `contextPath`: The URL context path under which the web application will be registered (defaults to WAR name).
* `enableSSL`: Determines whether the HTTPS connector should be created (defaults to `false`).
* `daemon`: Specifies whether the Tomcat server should run in the background. When true, this task completes as soon as the
server has started. When false, this task blocks until the Tomcat server is stopped (defaults to `false`).
* `keystoreFile`: The keystore file to use for SSL, if enabled (by default, a keystore will be generated).
* `httpProtocol`: The HTTP protocol handler class name to be used (defaults to `org.apache.coyote.http11.Http11Protocol`).
* `httpsProtocol`: The HTTPS protocol handler class name to be used (defaults to `org.apache.coyote.http11.Http11Protocol`).
* `ajpProtocol`: The AJP protocol handler class name to be used (defaults to `org.apache.coyote.ajp.AjpProtocol`).
* `users`: List of users with `username`, `password` and `roles`. Used to configure tomcat with basic authentication 
with these users.

### Example

The following example code shows how to change the default HTTP/HTTPS ports. To enable SSL we set the property `enableSSL` to `true`.
The web application will be accessible under the context path `sample-app`.

```groovy
tomcat {
    httpPort = 8090
    httpsPort = 8091
    enableSSL = true
    contextPath = 'sample-app'
    
    users {
        user {
            username = 'user1'
            password = '123456'
            roles = ['developers', 'admin']
        }

        user {
            username = 'user2'
            password = 'abcdef'
            roles = ['manager']
        }
    }
}
```

## Task properties

Furthermore, you can set the following optional task properties:

* `contextPath`: The URL context path your web application will be registered under (defaults to WAR name).
* `webDefaultXml`: The default web.xml. If it doesn't get defined an instance of `org.apache.catalina.servlets.DefaultServlet`
and `org.apache.jasper.servlet.JspServlet` will be set up.
* `additionalRuntimeResources`: Defines additional runtime JARs or directories that are not provided by the web application.
* `URIEncoding`: Specifies the character encoding used to decode the URI bytes by the HTTP Connector (defaults to `UTF-8`).
* `daemon`: Specifies whether the Tomcat server should run in the background. When true, this task completes as soon as the
server has started. When false, this task blocks until the Tomcat server is stopped (defaults to `false`).
* `configFile`: The path to the Tomcat context XML file (defaults to `src/main/webapp/META-INF/context.xml` for `tomcatRun`,
defaults to `META-INF/context.xml` within the WAR for `tomcatRunWar`).
* `outputFile`: The file to write Tomcat log messages to. If the file already exists new messages will be appended.
* `reloadable`: Forces context scanning if you don't use a context file (defaults to `true`).
* `keystorePass`: The keystore password to use for SSL, if enabled.
* `truststoreFile`: The truststore file to use for SSL, if enabled.
* `truststorePass`: The truststore password to use for SSL, if enabled.
* `clientAuth`: The clientAuth setting to use, values may be: `true`, `false` or `want` (defaults to `false`).

Note: `keystoreFile` and `truststoreFile` each require an instance of a `File` object e.g. `file("/path/my.file")`

### Example

In the following example code, we declare a custom context file for the task `tomcatRun`.

```groovy
tomcatRun.configFile = file('context.xml')
```

To configure the Jasper compiler task you can choose to set the following properties within the `jasper` closure of the
`tomcat` extension:

* `validateXml`: Determines whether `web.xml` should be validated (defaults to `null`).
* `validateTld`: Determines whether `web.xml` should be validated (defaults to `null`).
* `uriroot`: The web application root directory (defaults to `src/main/webapp`).
* `webXmlFragment`: The generated web XML fragment file to be referenced by your `web.xml` file.
* `addWebXmlMappings`: Automatically add the generated web XML fragment to the `web.xml` file.  Caution: this will modify the `web.xml` file in the project, not the build directory.
* `outputDir`: The output directory the compiled JSPs will end up in (defaults to `build/jasper`).
* `classdebuginfo`: Should the class file be compiled with debugging information (defaults to `true`).
* `compiler`: Which compiler Ant should use to compile JSP pages. See the Ant documentation for more information. If the value is not set, then the default Eclipse JDT Java compiler will be used instead of using Ant. No default value.
* `compilerSourceVM`: What JDK version are the source files compatible with (defaults to `1.6`).
* `compilerTargetVM`: What JDK version are the generated files compatible with (defaults to `1.6`).
* `poolingEnabled`: Determines whether tag handler pooling is enabled. This is a compilation option. It will not alter the behaviour of JSPs that have already been compiled (defaults to `true`).
* `errorOnUseBeanInvalidClassAttribute`: Should Jasper issue an error when the value of the class attribute in an useBean action is not a valid bean class (defaults to `true`).
* `genStringAsCharArray`: Should text strings be generated as char arrays, to improve performance in some cases (defaults to `false`).
* `ieClassId`: The class-id value to be sent to Internet Explorer when using `<jsp:plugin>` tags (defaults to `clsid:8AD9C840-044E-11D1-B3E9-00805F499D93`).
* `javaEncoding`: Java file encoding to use for generating java source files (defaults to `UTF8`).
* `trimSpaces`: Should white spaces in template text between actions or directives be trimmed (defaults to `TrimSpaces.TRUE`).
* `xpoweredBy`: Determines whether X-Powered-By response header is added by generated servlet (defaults to `false`).

### Example

```groovy
tomcat {
    jasper {
        validateXml = true
        webXmlFragment = file("$webAppDir/WEB-INF/generated_web.xml")
        outputDir = file("$webAppDir/WEB-INF/src")
    }
}
```

## FAQ

**I get a compile exception when calling a JSP. Is there something I am missing?**

The exception you might see is probably similar to this one: `org.apache.jasper.JasperException: Unable to compile class for JSP`.
Tomcat 7.x and 8.x requires you to have [Eclipse ECJ 3.6.x](http://www.eclipse.org/jdt/core/) in your the classpath. However, this
version of the dependency does not exist in Maven Central. You'll have to download that dependency and put it in your own
repository or define a repository on your local disk where you can drop it in. Here's an example:

```groovy
repositories {
     flatDir name: 'localRepository', dirs: 'lib'
}
```

**Why do I get a `java.lang.ClassCastException` on `javax.servlet.Servlet`?**

Tomcat is very sensitive to having multiple versions of the dependencies `javax.servlet:servlet-api` and `javax.servlet:jsp-api`
in its classpath. By default they already get pulled in as transitive dependencies of the embedded Tomcat libraries. The
exception you might see looks similar to this one:

```
java.lang.ClassCastException: org.springframework.web.servlet.DispatcherServlet cannot be cast to javax.servlet.Servlet
        at org.apache.catalina.core.StandardWrapper.loadServlet(StandardWrapper.java:1062)
        at org.apache.catalina.core.StandardWrapper.load(StandardWrapper.java:1010)
        at org.apache.catalina.core.StandardContext.loadOnStartup(StandardContext.java:4935)
        at org.apache.catalina.core.StandardContext$3.call(StandardContext.java:5262)
        at org.apache.catalina.core.StandardContext$3.call(StandardContext.java:5257)
        at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)
        at java.util.concurrent.FutureTask.run(FutureTask.java:138)
        at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
        at java.lang.Thread.run(Thread.java:662)
```

To fix this make sure you define your JSP and Servlet module dependencies with the scope `providedCompile` like this:

```groovy
providedCompile 'javax.servlet:servlet-api:2.5',
                'javax.servlet:jsp-api:2.0'
```

**How do I remote debug my Tomcat started up by the plugin?**

If you want to be able to debug your application remotely you have to set the following JVM options in your `GRADLE_OPTS`
environment variable before starting up the container. The port number you choose is up to you.

```
-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005
```

Tomcat will then listen on the specified port for incoming remote debugging connections. When starting up the container
you should see the following message:

```
Listening for transport dt_socket at address: 5005
```

Check your IDE documentation on how to configure connecting to the remote debugging port.

* [IntelliJ Remote Run/Debug Configuration](http://www.jetbrains.com/idea/webhelp/run-debug-configuration-remote.html)
* [Eclipse Remote Debugging](http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Fconcepts%2Fcremdbug.htm)

**My Tomcat container needs to use a JNDI datasource. How do I set up my project?**

First of all you got to make sure to declare the connection pool dependency using the `tomcat` configuration.

**Tomcat 6.0.x:**

```groovy
def tomcatVersion = '6.0.35'
tomcat "org.apache.tomcat:dbcp:${tomcatVersion}"
```

See [coordinates on Maven Central](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.apache.tomcat%22%20AND%20a%3A%22dbcp%22) for details.

**Later versions:**

```groovy
def tomcatVersion = '9.0.8'
tomcat "org.apache.tomcat:tomcat-dbcp:${tomcatVersion}"
```

See [coordinates on Maven Central](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.apache.tomcat%22%20AND%20a%3A%22tomcat-dbcp%22) for details.

If you decide to go with the default settings place your `context.xml` in the directory `src/main/webapp/META-INF`. To
set a custom location you can use the convention property `configFile`. Here's an example on how to set it for the tasks
`tomcatRun` and `tomcatRunWar`.

```groovy
[tomcatRun, tomcatRunWar]*.configFile = file('context.xml')
```

Please refer to the [Tomcat documentation](http://tomcat.apache.org/tomcat-7.0-doc/config/context.html#Defining_a_context) for a list
of context attributes. The following example shows how to set up a MySQL JNDI datasource.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Resource name="jdbc/mydatabase"
              auth="Container"
              type="javax.sql.DataSource"
              username="superuser"
              password="secretpasswd"
              driverClassName="com.mysql.jdbc.Driver"
              url="jdbc:mysql://localhost:3306/mydb"
              validationQuery="select 1"
              maxActive="10"
              maxIdle="4"/>
</Context>
```

**How do I use hot code deployment with the plugin?**

The plugin provides out-of-the-box support for swapping out byte code through the property `reloadable`. By default this option
is turned out so you don't need any additional configuration changes. All you need to do is to have a running instance of the
container initiated by `tomcatRun`. Fire up your favorite editor, change a production source file, save it and recompile your
sources in another terminal via `gradle compileJava`. After a couple of seconds the context is reloaded and you should see the
behavior reflected in the terminal window running the container:

```
Reloading Context with name [/myapp] has started
Reloading Context with name [/myapp] is completed
```

Alternatively, you can use other commericial byte code swap technologies. The configuration is usually product-specific.
Please refer to the product's documentation on how to set it up for your project. The following section describes how to set up Gradle and the plugin with [JRebel](http://zeroturnaround.com/jrebel/).
First of all download [JRebel](http://zeroturnaround.com/jrebel/current/), install it on your machine and set up the [license](http://zeroturnaround.com/reference-manual/install.html#install-1.3).
To tell JRebel which directory to scan for changed byte code you need to create a [rebel.xml](file://localhost/Users/benjamin/dev/tools/jrebel/doc/app.html#app) file. In
your web module place the file under `build/classes/main` so it can be loaded by the Tomcat plugin. For creating the configuration of the file
the [Gradle JRebel plugin](http://zeroturnaround.com/blog/jrebel-gradle-plugin-beta/) comes in handy. It's not required
to use the plugin. You can also decide to create the configuration by hand. Keep in mind that `gradle clean` will delete the file. 
For setting up JRebel in a multi-module project scenario please refer to the documentation. The following code snippet shows an example
`rebel.xml` file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<application xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.zeroturnaround.com"
            xsi:schemaLocation="http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd">
    <classpath>
        <dir name="/Users/ben/dev/projects/mywebproject/build/classes/main">
        </dir>
    </classpath>

    <web>
        <link target="/">
            <dir name="/Users/ben/dev/projects/mywebproject/src/main/webapp">
            </dir>
        </link>
    </web>
</application>
```

Edit your Gradle startup script and add the following line to it to tell Gradle to [use the JRebel agent](http://zeroturnaround.com/reference-manual/server.html#server-4.5.36).
Please make sure to set the environment variable `REBEL_HOME` that points to your JRebel installation directory.

```
JAVA_OPTS="-javaagent:$REBEL_HOME/jrebel.jar $JAVA_OPTS"
```

On startup of your web module using `gradle tomcatRun` you should see information about the JRebel license being used and
the directories being scanned for changes. For our example `rebel.xml` file it would look like this:

```
JRebel: Directory '/Users/ben/dev/projects/mywebproject/build/classes/main' will be monitored for changes.
JRebel: Directory '/Users/ben/dev/projects/mywebproject/src/main/webapp' will be monitored for changes.
```

If a file has been recompiled JRebel indicates this by writing it to the console like this:

```
JRebel: Reloading class 'de.muschko.web.controller.TestController'.
```

**In need to run in-container integration tests as part of my build. What needs to be done?**

Usually unit and integration tests are kept separate by convention. One convention could be to name the test source
files differently e.g. integration tests always end with the suffix `IntegrationTest`, unit test files end with `Test`.
By doing that you can run them separately. For running the integration tests you will want to run the Tomcat task as daemon
thread and shut it down once your tests are done. The following example demonstrates how to set up a Gradle task that provides this
functionality. Of course this is only one way of doing it. The following example requires Gradle >= 1.7:

```groovy
apply plugin: 'com.bmuschko.tomcat-base'

ext {
    tomcatStopPort = 8081
    tomcatStopKey = 'stopKey'
}

task integrationTomcatRun(type: com.bmuschko.gradle.tomcat.tasks.TomcatRun) {
    stopPort = tomcatStopPort
    stopKey = tomcatStopKey
    daemon = true
}

task integrationTomcatStop(type: com.bmuschko.gradle.tomcat.tasks.TomcatStop) {
    stopPort = tomcatStopPort
    stopKey = tomcatStopKey
}

task integrationTest(type: Test) {
    include '**/*IntegrationTest.*'
    dependsOn integrationTomcatRun
    finalizedBy integrationTomcatStop
}

test {
    exclude '**/*IntegrationTest.*'
}
```

**How do I add JAR files or directories that are not part of my web application source code?**

Every task of type `AbstractTomcatRun` exposes a property named `additionalRuntimeResources` that is used to mixed in
with the web application runtime classpath.

```groovy
[tomcatRun, tomcatRunWar].each { task ->
    task.additionalRuntimeResources << file('/Users/bmuschko/config/props')
    task.additionalRuntimeResources << file('/Users/bmuschko/ext/jars/my.jar')
}
```
