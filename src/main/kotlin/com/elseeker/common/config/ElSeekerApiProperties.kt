package com.elseeker.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.el-seeker")
data class ElSeekerApiProperties(
    val url: String,
    val apiKey: String
)
