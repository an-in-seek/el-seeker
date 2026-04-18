package com.elseeker.analytics.config

import com.elseeker.analytics.adapter.input.web.SiteVisitTrackingInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class AnalyticsWebConfig(
    private val siteVisitTrackingInterceptor: SiteVisitTrackingInterceptor,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(siteVisitTrackingInterceptor)
            .addPathPatterns("/", "/web/**")
            .excludePathPatterns(
                "/web/admin/**",
                "/web/auth/**",
                "/error",
            )
    }
}
