package com.bmuschko.gradle.tomcat.tasks

class TomcatRunAlways extends Tomcat {
    TomcatRunAlways() {
        // No matter what the inputs and outputs make sure that run tasks are never up-to-date
        outputs.upToDateWhen {
            false
        }
    }
}
