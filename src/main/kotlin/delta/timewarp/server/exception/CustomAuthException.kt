package delta.timewarp.server.exception

import org.springframework.http.HttpStatus

data class CustomAuthException(val status: HttpStatus, override val message: String?) : Exception()
