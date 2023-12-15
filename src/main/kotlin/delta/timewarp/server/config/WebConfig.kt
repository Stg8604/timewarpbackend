package delta.timewarp.server.config

import delta.timewarp.server.interceptors.AdvanceTurnInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {

    @Value("\${cors.allowed-origin}")
    private lateinit var allowedOrigin: String

    @Autowired lateinit var advanceTurnInterceptor: AdvanceTurnInterceptor

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowedOrigins(allowedOrigin).allowCredentials(true)
    }
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(advanceTurnInterceptor).addPathPatterns("/api/user/action/*")
    }
}
