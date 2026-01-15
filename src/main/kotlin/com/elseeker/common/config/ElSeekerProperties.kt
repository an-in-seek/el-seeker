package com.elseeker.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "el-seeker")
data class ElSeekerProperties(
    val jwt: Jwt,
    val api: Api,
) {

    data class Jwt(
        val secret: String,
        val accessTokenTtl: Duration,
        val refreshTokenTtl: Duration
    )

    data class Api(
        val baseUrl: String,
        val apiKey: String
    )
}