package com.elseeker.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.the-bible")
data class TheBibleApiProperties(
    val url: String,
    val apiKey: String
)
