package com.elseeker.common.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object RequestTimeZoneContext {
    private val zoneHolder = ThreadLocal<ZoneId>()

    fun set(zoneId: ZoneId) {
        zoneHolder.set(zoneId)
    }

    fun get(): ZoneId = zoneHolder.get() ?: ZoneOffset.UTC

    fun clear() {
        zoneHolder.remove()
    }
}

class AcceptLanguageTimeZoneResolver {
    private val languageTagZones: Map<String, ZoneId> = mapOf(
        "ko-kr" to ZoneId.of("Asia/Seoul"),
        "ko" to ZoneId.of("Asia/Seoul"),
        "ja-jp" to ZoneId.of("Asia/Tokyo"),
        "ja" to ZoneId.of("Asia/Tokyo"),
        "zh-cn" to ZoneId.of("Asia/Shanghai"),
        "zh-tw" to ZoneId.of("Asia/Taipei"),
        "zh" to ZoneId.of("Asia/Shanghai"),
        "en-us" to ZoneId.of("America/New_York"),
        "en-gb" to ZoneId.of("Europe/London"),
        "en" to ZoneOffset.UTC
    )

    fun resolve(acceptLanguage: String?): ZoneId {
        if (acceptLanguage.isNullOrBlank()) {
            return ZoneOffset.UTC
        }
        val range = runCatching { Locale.LanguageRange.parse(acceptLanguage).firstOrNull() }.getOrNull()
            ?: return ZoneOffset.UTC
        val tag = range.range.lowercase(Locale.ROOT)
        val locale = Locale.forLanguageTag(range.range)
        val localeTag = locale.toLanguageTag().lowercase(Locale.ROOT)

        return languageTagZones[tag]
            ?: languageTagZones[localeTag]
            ?: languageTagZones[locale.language.lowercase(Locale.ROOT)]
            ?: ZoneOffset.UTC
    }
}

class RequestTimeZoneInterceptor(
    private val resolver: AcceptLanguageTimeZoneResolver = AcceptLanguageTimeZoneResolver()
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val zoneId = resolver.resolve(request.getHeader("Accept-Language"))
        RequestTimeZoneContext.set(zoneId)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        RequestTimeZoneContext.clear()
    }
}

class ClientLocalDateTimeSerializer : JsonSerializer<LocalDateTime>() {
    override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        val zoneId = RequestTimeZoneContext.get()
        val zonedValue = value.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId)
        gen.writeString(zonedValue.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }
}

@Configuration
class RequestTimeZoneConfig : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(RequestTimeZoneInterceptor())
    }

    @Bean
    fun clientTimeZoneModule(): Module {
        val module = SimpleModule()
        module.addSerializer(LocalDateTime::class.java, ClientLocalDateTimeSerializer())
        return module
    }
}
