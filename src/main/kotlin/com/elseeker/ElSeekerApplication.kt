package com.elseeker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ElSeekerApplication

fun main(args: Array<String>) {
    runApplication<ElSeekerApplication>(*args)
}
