package delta.timewarp.server.exception

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class RestExceptionHandler() :
    ResponseEntityExceptionHandler() {

    private val logger = LoggerFactory.getLogger(delta.timewarp.server.exception.RestExceptionHandler::class.java)

    @ExceptionHandler(ConstraintViolationException::class)
    protected fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(mapOf("message" to ex.constraintViolations.joinToString(", ")))
    }

    @ExceptionHandler(delta.timewarp.server.exception.CustomAuthException::class)
    protected fun handleCustomException(error: delta.timewarp.server.exception.CustomAuthException): ResponseEntity<Any> {
        return ResponseEntity.status(error.status).body(
            delta.timewarp.server.dtos.ErrorMessageDTO(
                error.message.toString()
            )
        )
    }

    @ExceptionHandler(delta.timewarp.server.exception.CustomException::class)
    protected fun handleCustomException(error: delta.timewarp.server.exception.CustomException): ResponseEntity<Any> {
        return ResponseEntity.status(error.status).body(
            delta.timewarp.server.dtos.ErrorMessageDTO(
                error.message.toString()
            )
        )
    }

    @ExceptionHandler(RuntimeException::class)
    protected fun handleRuntimeException(error: Exception): ResponseEntity<delta.timewarp.server.dtos.ErrorMessageDTO> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(delta.timewarp.server.dtos.ErrorMessageDTO("There seems to be an issue"))

    }
}
