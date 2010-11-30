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

import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.IConventionAware
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.WarPluginConvention
import org.gradle.api.tasks.ConventionValue
import org.gradle.api.tasks.bundling.War

/**
 * <p>A {@link Plugin} which extends the {@link WarPlugin} to add tasks which run the web application using an embedded
 * Tomcat web container.</p>
 *
 * @author Benjamin Muschko
 */
class TomcatPlugin implements Plugin<Project> {
    static final String TOMCAT_RUN = "tomcatRun"
    static final String TOMCAT_RUN_WAR = "tomcatRunWar"
    static final String TOMCAT_STOP = "tomcatStop"
    static final String HTTP_PORT_SYSPROPERTY = "tomcat.http.port"
    static final String STOP_PORT_SYSPROPERTY = "tomcat.stop.port"
    static final String STOP_KEY_SYSPROPERTY = "tomcat.stop.key"

    @Override
    public void apply(Project project) {
        project.plugins.apply(WarPlugin.class);
        TomcatPluginConvention tomcatConvention = new TomcatPluginConvention()
        project.convention.plugins.tomcat = tomcatConvention

        configureMappingRules(project, tomcatConvention)
        configureTomcatRun(project)
        configureTomcatRunWar(project)
        configureTomcatStop(project, tomcatConvention)
    }

    private void configureMappingRules(final Project project, final TomcatPluginConvention tomcatConvention) {
        project.getTasks().withType(AbstractTomcatRunTask.class).whenTaskAdded(new Action<AbstractTomcatRunTask>() {
            @Override
            public void execute(AbstractTomcatRunTask abstractTomcatRunTask) {
                configureAbstractTomcatTask(project, tomcatConvention, abstractTomcatRunTask);
            }
        });
    }

    private void configureAbstractTomcatTask(final Project project, final TomcatPluginConvention tomcatConvention, AbstractTomcatRunTask tomcatTask) {
        tomcatTask.reloadable = true
        tomcatTask.getConventionMapping().map("contextPath", new ConventionValue() {
            @Override
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                ((War)project.getTasks().getByName(WarPlugin.WAR_TASK_NAME)).getBaseName()
            }
        });
        tomcatTask.getConventionMapping().map("httpPort", new ConventionValue() {
            @Override
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                getHttpPort(tomcatConvention)
            }
        });
        tomcatTask.getConventionMapping().map("stopPort", new ConventionValue() {
            @Override
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                getStopPort(tomcatConvention)
            }
        });
        tomcatTask.getConventionMapping().map("stopKey", new ConventionValue() {
            @Override
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                getStopKey(tomcatConvention)
            }
        });
    }

    private void configureTomcatRun(final Project project) {
        project.getTasks().withType(TomcatRun.class).whenTaskAdded(new Action<TomcatRun>() {
            @Override
            public void execute(TomcatRun tomcatRun) {
                tomcatRun.getConventionMapping().map("classpath", new ClasspathConventionValue(project))
                tomcatRun.getConventionMapping().map("webAppSourceDirectory", new WebAppSourceDirectoryConventionValue(project))
            }
        })

        TomcatRun tomcatRun = project.getTasks().add(TOMCAT_RUN, TomcatRun.class)
        tomcatRun.setDescription("Uses your files as and where they are and deploys them to Tomcat.")
        tomcatRun.setGroup(WarPlugin.WEB_APP_GROUP)
    }

    private void configureTomcatRunWar(final Project project) {
        project.getTasks().withType(TomcatRunWar.class).whenTaskAdded(new Action<TomcatRunWar>() {
            @Override
            public void execute(TomcatRunWar tomcatRunWar) {
                tomcatRunWar.dependsOn(WarPlugin.WAR_TASK_NAME);
                tomcatRunWar.getConventionMapping().map("webApp", new WebAppConventionValue(project))
            }
        })

        TomcatRunWar tomcatRunWar = project.getTasks().add(TOMCAT_RUN_WAR, TomcatRunWar.class);
        tomcatRunWar.setDescription("Assembles the webapp into a war and deploys it to Tomcat.");
        tomcatRunWar.setGroup(WarPlugin.WEB_APP_GROUP);
    }

    private void configureTomcatStop(final Project project, final TomcatPluginConvention tomcatConvention) {
        TomcatStop tomcatStop = project.getTasks().add(TOMCAT_STOP, TomcatStop.class)
        tomcatStop.setDescription("Stops Tomcat.");
        tomcatStop.setGroup(WarPlugin.WEB_APP_GROUP);
        tomcatStop.getConventionMapping().map("stopPort", new ConventionValue() {
            @Override
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                getStopPort(tomcatConvention)
            }
        })
        tomcatStop.getConventionMapping().map("stopKey", new ConventionValue() {
            @Override
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                getStopKey(tomcatConvention)
            }
        })
    }

    private class ClasspathConventionValue implements ConventionValue {
        private Project project

        public ClasspathConventionValue(Project project) {
            this.project = project
        }

        @Override
        public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
            ((War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME)).getClasspath()
        }
    }

    private class WebAppSourceDirectoryConventionValue implements ConventionValue {
        private Project project

        public WebAppSourceDirectoryConventionValue(Project project) {
            this.project = project
        }

        @Override
        public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
            getWarConvention(project).getWebAppDir()
        }
    }

    private class WebAppConventionValue implements ConventionValue {
        private Project project

        public WebAppConventionValue(Project project) {
            this.project = project
        }

        @Override
        public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
            ((War)project.getTasks().getByName(WarPlugin.WAR_TASK_NAME)).getArchivePath()
        }
    }

    private Integer getHttpPort(final TomcatPluginConvention tomcatConvention) {
        String httpPortSystemProperty = System.getProperty(HTTP_PORT_SYSPROPERTY)

        if(httpPortSystemProperty) {
            try {
                return httpPortSystemProperty.toInteger()
            }
            catch(NumberFormatException e) {
                throw new InvalidUserDataException("Bad HTTP port provided as system property: ${httpPortSystemProperty}", e)
            }
        }

        tomcatConvention.httpPort
    }

    private Integer getStopPort(final TomcatPluginConvention tomcatConvention) {
        String stopPortSystemProperty = System.getProperty(STOP_PORT_SYSPROPERTY)

        if(stopPortSystemProperty) {
            try {
                return stopPortSystemProperty.toInteger()
            }
            catch(NumberFormatException e) {
                throw new InvalidUserDataException("Bad stop port provided as system property: ${stopPortSystemProperty}", e)
            }
        }

        tomcatConvention.stopPort
    }

    private String getStopKey(final TomcatPluginConvention tomcatConvention) {
        String stopKeySystemProperty = System.getProperty(STOP_KEY_SYSPROPERTY)

        if(stopKeySystemProperty) {
            return stopKeySystemProperty
        }

        tomcatConvention.stopKey
    }

    JavaPluginConvention getJavaConvention(Project project) {
        project.convention.getPlugin(JavaPluginConvention.class)
    }

    WarPluginConvention getWarConvention(Project project) {
        project.convention.getPlugin(WarPluginConvention.class)
    }
}