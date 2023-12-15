package delta.timewarp.server.user.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import delta.timewarp.server.exception.CustomException

@Component
class CustomOAuth2FailureHandler : AuthenticationFailureHandler {

    @Value("\${frontend-domain}")
    private lateinit var frontenddomainurl: String

    private val logger: Logger = LoggerFactory.getLogger(CustomOAuth2FailureHandler::class.java)

    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        if (exception?.cause is CustomException) {
            response?.sendRedirect("$frontenddomainurl/oauth?error=${exception.cause?.message}")
        } else {
            response?.sendRedirect("$frontenddomainurl/oauth?error=Internal Server Error")
        }
    }
}
