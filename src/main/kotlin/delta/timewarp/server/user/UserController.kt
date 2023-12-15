package delta.timewarp.server.user

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import delta.timewarp.server.user.dtos.ActivateUserRequestDTO
import delta.timewarp.server.user.dtos.ForgotPasswordDTO
import delta.timewarp.server.user.dtos.LoginDTO
import delta.timewarp.server.user.dtos.RegisterDTO
import delta.timewarp.server.user.dtos.ResetPasswordDTO

@RestController
@RequestMapping("api")
class AuthController(
    @Autowired private val userService: UserService,
    @Autowired private val request: HttpServletRequest
) {

    @PostMapping("register")
    fun register(@RequestBody body: RegisterDTO): ResponseEntity<Any> {
        return userService.register(body, request.remoteAddr)
    }

    @PostMapping("login")
    fun login(@RequestBody body: LoginDTO, response: HttpServletResponse): ResponseEntity<Any> {
        return userService.login(body, response, request.remoteAddr)
    }

    @GetMapping("user")
    fun user(@CookieValue("jwt") jwt: String?): ResponseEntity<Any> {
        return userService.user(jwt)
    }

    @GetMapping("logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Any> {
        return userService.logout(response)
    }

    @PostMapping("/activate-user")
    fun activateUser(@RequestBody body: ActivateUserRequestDTO): ResponseEntity<Any> {
        return userService.activateUser(body)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody body: ForgotPasswordDTO): ResponseEntity<Any> {
        return userService.forgotPasswordEmail(body)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody body: ResetPasswordDTO): ResponseEntity<Any> {
        return userService.resetPassword(body)
    }
}

