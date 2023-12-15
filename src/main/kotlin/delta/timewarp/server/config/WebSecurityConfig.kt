package delta.timewarp.server.config

import delta.timewarp.server.user.UserService
import delta.timewarp.server.user.oauth2.CustomOAuth2FailureHandler
import delta.timewarp.server.user.oauth2.CustomOAuth2SuccessHandler
import delta.timewarp.server.user.oauth2.CustomOAuth2UserService
import delta.timewarp.server.user.oauth2.CustomOidcUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig {
    @Autowired private lateinit var userService: UserService

    @Autowired private lateinit var customOidcUserService: CustomOidcUserService

    @Autowired private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @Autowired private lateinit var customOAuth2SuccessHandler: CustomOAuth2SuccessHandler

    @Autowired private lateinit var customOAuth2FailureHandler: CustomOAuth2FailureHandler

    @Value("\${cors.enabled}")
    private val corsEnabled: Boolean = false

    @Bean
    fun filterChain(http: HttpSecurity?): SecurityFilterChain? {
        if (http != null) {
            http.invoke {
                csrf { disable() }
                oauth2Login {
                    userInfoEndpoint {
                        oidcUserService = customOidcUserService
                        userService = customOAuth2UserService
                    }
                    authenticationSuccessHandler = customOAuth2SuccessHandler
                    authenticationFailureHandler = customOAuth2FailureHandler
                }
                authorizeRequests { authorize(HttpMethod.OPTIONS, "/**", permitAll) }
                cors { if (!corsEnabled) disable() }
                sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            }
            return http.build()
        }
        return null
    }
}
