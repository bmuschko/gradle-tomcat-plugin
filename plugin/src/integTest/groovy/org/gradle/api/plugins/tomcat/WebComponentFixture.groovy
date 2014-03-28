package org.gradle.api.plugins.tomcat

class WebComponentFixture {
    void createServlet2xWebApp(File projectDir) {
        File javaSrcDir = createJavaSourceDirectory(projectDir)
        createSimpleServlet(javaSrcDir)
        createForwardServlet(javaSrcDir)
        File webInfDir = createWebInfDirectory(projectDir)
        createWebXml(webInfDir)
        File jspDir = createJspDirectory(projectDir)
        createJsp(jspDir)
    }

    private File createJavaSourceDirectory(File projectDir) {
        File javaSrcDir = new File(projectDir, 'src/main/java/org/gradle/web')
        boolean success = javaSrcDir.mkdirs()

        if(!success) {
            throw new IOException("Failed to create temporary Java source directory '$javaSrcDir.canonicalPath'")
        }

        javaSrcDir
    }


    private void createSimpleServlet(File srcDir) {
        File servletFile = new File(srcDir, 'SimpleServlet.java')
        servletFile << """
package org.gradle.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SimpleServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().print("Hello World!");
    }
}
"""
    }

    private void createForwardServlet(File srcDir) {
        File servletFile = new File(srcDir, 'ForwardServlet.java')
        servletFile << """
package org.gradle.web;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ForwardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/jsp/helloworld.jsp");
        requestDispatcher.forward(request, response);
    }
}
"""
    }

    private File createWebInfDirectory(File projectDir) {
        File webInfDir = new File(projectDir, 'src/main/webapp/WEB-INF')
        boolean success = webInfDir.mkdirs()

        if(!success) {
            throw new IOException("Failed to create temporary WEB-INF directory '$webInfDir.canonicalPath'")
        }

        webInfDir
    }

    private void createWebXml(File webInfDir) {
        File webXmlFile = new File(webInfDir, 'web.xml')
        webXmlFile << """<?xml version="1.0" encoding="UTF-8"?>
<web-app version="2.5" xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">
    <servlet>
        <servlet-name>SimpleServlet</servlet-name>
        <servlet-class>org.gradle.web.SimpleServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet>
        <servlet-name>ForwardServlet</servlet-name>
        <servlet-class>org.gradle.web.ForwardServlet</servlet-class>
        <load-on-startup>2</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>SimpleServlet</servlet-name>
        <url-pattern>/simple</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>ForwardServlet</servlet-name>
        <url-pattern>/forward</url-pattern>
    </servlet-mapping>
</web-app>
"""
    }

    private File createJspDirectory(File projectDir) {
        File jspDir = new File(projectDir, 'src/main/webapp/jsp')
        boolean success = jspDir.mkdirs()

        if(!success) {
            throw new IOException("Failed to create temporary JSP directory '$jspDir.canonicalPath'")
        }

        jspDir
    }

    private void createJsp(File jspDir) {
        File jspFile = new File(jspDir, 'helloworld.jsp')
        jspFile << 'Hello World!'
    }
}
