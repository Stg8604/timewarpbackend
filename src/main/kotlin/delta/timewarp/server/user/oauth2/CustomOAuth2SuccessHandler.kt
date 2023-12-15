package delta.timewarp.server.user.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import delta.timewarp.server.exception.CustomException
import delta.timewarp.server.user.UserService

@Component
class CustomOAuth2SuccessHandler(@Autowired private val authService: UserService) :
    AuthenticationSuccessHandler {

    @Value("\${frontend-domain}")
    private lateinit var frontenddomainurl: String

    private val logger: Logger = LoggerFactory.getLogger(
        CustomOAuth2SuccessHandler::class.java)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val principal = authentication?.principal
        if (principal is OAuth2AuthenticatedPrincipal) {
            val attributes = principal.attributes
            val email = attributes["email"] as String
            val username = attributes["given_name"] as String
            try {
                val token = authService.oAuth2Login(email, username)
                response?.sendRedirect("$frontenddomainurl/oauth?jwt=$token")
            } catch (e: CustomException) {
                response?.sendRedirect("$frontenddomainurl/oauth?error=${e.message}")
            } catch (e: Exception) {
                logger.error("Error while logging in: ${e.message}")
                response?.sendRedirect("$frontenddomainurl/oauth?error=Internal Server Error")
            }
        } else {
            response?.sendRedirect("$frontenddomainurl/oauth?error=Invalid Login, please try again later")
        }
    }
}
