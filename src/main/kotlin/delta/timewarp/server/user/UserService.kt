package delta.timewarp.server.user

import delta.timewarp.server.dtos.ErrorMessageDTO
import delta.timewarp.server.dtos.MessageDTO
import delta.timewarp.server.exception.CustomAuthException
import delta.timewarp.server.exception.CustomException
import delta.timewarp.server.sendgrid.SendGridService
import delta.timewarp.server.user.activateUser.ActivateUserEntity
import delta.timewarp.server.user.activateUser.ActivateUserRepository
import delta.timewarp.server.user.dtos.ActivateUserRequestDTO
import delta.timewarp.server.user.dtos.ForgotPasswordDTO
import delta.timewarp.server.user.dtos.LoginDTO
import delta.timewarp.server.user.dtos.RegisterDTO
import delta.timewarp.server.user.dtos.ResetPasswordDTO
import delta.timewarp.server.user.enums.LoginType
import delta.timewarp.server.user.forgotPassword.ForgotPasswordEntity
import delta.timewarp.server.user.forgotPassword.ForgotPasswordRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.Date
import java.util.UUID

@Service
class UserService(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val activateUserRepository: ActivateUserRepository,
    @Autowired private val sendGridService: SendGridService,
    @Autowired private val forgotPasswordRepository: ForgotPasswordRepository,
    private val captchaService: ReCaptchaService
) {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${production}")
    private lateinit var production: String

    fun checkEmailValidity(email: String): Boolean {
        val emailRegex =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        return email.matches(Regex(emailRegex))
    }

    fun encodePassword(password: String): String {
        val passwordEncoder = BCryptPasswordEncoder()
        return passwordEncoder.encode(password)
    }

    fun generateJwtToken(userId: UUID): String {
        return Jwts.builder()
            .setIssuer(userId.toString())
            .setExpiration(Date(System.currentTimeMillis() + 60 * 60 * 24 * 7 * 1000))
            .signWith(SignatureAlgorithm.HS256, secret.toByteArray())
            .compact()
    }

    fun register(body: RegisterDTO, ip: String): ResponseEntity<Any> {
        if (listOf(body.username, body.email, body.password).any { it.isBlank() }) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Please fill in all fields")
        }
        if (body.username.length < 3 || body.username.length > 15) {
            throw CustomAuthException(
                HttpStatus.BAD_REQUEST,
                "Username should be between 3-15 characters"
            )
        }
        if (!checkEmailValidity(body.email)) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Invalid email address!")
        }
        if (userRepository.findByEmail(body.email) != null) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Email is already registered")
        }
        val captchaValid = captchaService.validateCaptcha(body.token, ip)
        if (!captchaValid) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Captcha Failed, try again later")
        }
        val user =
            UserEntity(
                id = UUID.randomUUID(),
                email = body.email,
                username = body.username,
                password = encodePassword(body.password),
                loginType = LoginType.PASSWORD,
                isEnabled = !production.toBoolean()
            )
        userRepository.save(user)
        val userId = userRepository.findByEmail(body.email)!!.id
        createActivationUser(userId, user.username, user.email)
        val forgotPasswordUser =
            ForgotPasswordEntity(
                id = UUID.randomUUID(),
                userId = userId,
                email = body.email,
                token = "",
                expiration = Date(System.currentTimeMillis())
            )
        forgotPasswordRepository.save(forgotPasswordUser)

        return ResponseEntity.ok(MessageDTO("Registered successfully! You can login now!"))
    }

    fun createActivationUser(userId: UUID, name: String, email: String) {
        val token = UUID.randomUUID().toString()
        val activationUser =
            ActivateUserEntity(
                id = UUID.randomUUID(),
                userId = userId,
                token = token,
                expiration = Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)
            )
        activateUserRepository.save(activationUser)
        sendGridService.activateUserEmail(userId, token, name, email)
    }

    fun activateUser(body: ActivateUserRequestDTO): ResponseEntity<Any> {
        val user =
            userRepository.findById(UUID.fromString(body.userId)).orElseThrow {
                throw CustomException(HttpStatus.BAD_REQUEST, "User not Found")
            }
        val unactivatedUser: ActivateUserEntity =
            activateUserRepository.findByToken(body.token)
                ?: throw CustomAuthException(HttpStatus.BAD_REQUEST, "Activation Token Not Found!")
        if (unactivatedUser.expiration < Date(System.currentTimeMillis())) {
            userRepository.delete(user)
            throw CustomAuthException(
                HttpStatus.BAD_REQUEST,
                "Sorry! You cannot login. Activation Token Expired!"
            )
        }
        user.isEnabled = true
        userRepository.save(user)
        activateUserRepository.delete(unactivatedUser)
        return ResponseEntity.ok("Verified")
    }

    fun login(body: LoginDTO, response: HttpServletResponse, ip: String): ResponseEntity<Any> {
        if (listOf(body.email, body.password).any { it.isBlank() }) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Please fill in all fields")
        }
        val user =
            userRepository.findByEmail(body.email)
                ?: throw CustomAuthException(HttpStatus.BAD_REQUEST, "User not found!")
        if (!user.comparePassword(body.password)) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Invalid Credentials!")
        }
        if (!user.isEnabled) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Please Verify Your Email First")
        }
        if (user.loginType != LoginType.PASSWORD) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Invalid login type!")
        }
        val captchaValid = captchaService.validateCaptcha(body.token, ip)
        if (!captchaValid) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Captcha Failed, try again later")
        }
        return ResponseEntity.ok(generateJwtToken(user.id))
    }

    fun forgotPasswordEmail(body: ForgotPasswordDTO): ResponseEntity<Any> {
        val forgotPasswordUser =
            forgotPasswordRepository.findByEmail(body.email)
                ?: throw CustomAuthException(HttpStatus.BAD_REQUEST, "User Not Found")
        val user =
            userRepository.findByEmail(body.email)
                ?: throw CustomAuthException(HttpStatus.BAD_REQUEST, " Please Register First")
        if (!user.isEnabled) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, " Please Verify Your Email First")
        }

        if (user.loginType == LoginType.GOOGLE_OAUTH) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, " Can't Change Password!")
        }

        forgotPasswordUser.token = UUID.randomUUID().toString()
        forgotPasswordUser.expiration = Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)
        forgotPasswordRepository.save(forgotPasswordUser)
        sendGridService.forgotUserEmail(forgotPasswordUser.token, forgotPasswordUser.email)

        return ResponseEntity.ok("Succesfully Changed Password!")
    }

    fun resetPassword(body: ResetPasswordDTO): ResponseEntity<Any> {
        val resetUserPassword =
            forgotPasswordRepository.findByToken(body.token)
                ?: throw CustomAuthException(HttpStatus.BAD_REQUEST, "User Not found!")
        val user =
            userRepository.findByEmail(resetUserPassword.email)
                ?: throw CustomAuthException(HttpStatus.BAD_REQUEST, "User Not found")
        if (resetUserPassword.expiration < Date(System.currentTimeMillis())) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Expiration Time Exceeded")
        }

        user.password = encodePassword(body.password)
        userRepository.save(user)
        return ResponseEntity.ok("Password Changed Successfully!")
    }

    fun oAuth2Login(email: String, username: String): String {
        val user = userRepository.findByEmail(email)
        if (user != null && user.loginType != LoginType.GOOGLE_OAUTH) {
            throw CustomAuthException(HttpStatus.BAD_REQUEST, "Invalid login type!")
        }
        val userId = user?.id ?: createUserWithOAuth(email, username)
        return generateJwtToken(userId)
    }

    fun createUserWithOAuth(email: String, username: String): UUID {
        val user =
            UserEntity(
                id = UUID.randomUUID(),
                password = encodePassword(UUID.randomUUID().toString()),
                email = email,
                username = username,
                loginType = LoginType.GOOGLE_OAUTH,
                isEnabled = true
            )
        userRepository.save(user)

        val userId = userRepository.findByEmail(email)!!.id
        return userId
    }

    fun user(jwt: String?): ResponseEntity<Any> {
        try {
            if (jwt == null) {
                throw CustomAuthException(HttpStatus.UNAUTHORIZED, "Unauthenticated")
            }
            val body = Jwts.parser().setSigningKey(secret.toByteArray()).parseClaimsJws(jwt).body
            val user =
                userRepository.findById(UUID.fromString(body.issuer)).orElseThrow {
                    throw CustomAuthException(HttpStatus.BAD_REQUEST, "User not found")
                }
            return ResponseEntity.ok("Authenticated!")
        } catch (e: Exception) {
            if (e is CustomAuthException) {
                return ResponseEntity.status(e.status).body(ErrorMessageDTO(e.message.toString()))
            }
            return ResponseEntity.internalServerError().body(ErrorMessageDTO("Internal Server Error"))
        }
    }

    fun logout(response: HttpServletResponse): ResponseEntity<Any> {
        val cookie = Cookie("jwt", "")
        cookie.maxAge = 0
        response.addCookie(cookie)
        return ResponseEntity.ok("Successfully logged out")
    }

    @Bean
    fun jwtFilter(): FilterRegistrationBean<JWTFilter> {
        val filter = JWTFilter(userRepository, secret)
        val registration = FilterRegistrationBean(filter)
        registration.addUrlPatterns(
            "/api/logout",
            "/api/user/action/*",
            "/api/game",
            "/api/getLeaderBoard",
            "/api/news"
        )
        return registration
    }
}

class JWTFilter(
    @Autowired private val userRepository: UserRepository,
    @Value("\${jwt.secret}") private val secret: String
) : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        // Get the JWT from the request header
        val httpRequest = request as HttpServletRequest
        // Get the JWT from the cookie
        val jwt = httpRequest.cookies?.find { it.name == "jwt" }?.value

        // If the JWT is not present, return an error
        if (jwt == null) {
            val httpResponse = response as HttpServletResponse
            httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
            httpResponse.writer.write("Unauthorized: missing JWT")
            return
        }
        try {
            // Parse the JWT and validate it
            val claims = Jwts.parser().setSigningKey(secret.toByteArray()).parseClaimsJws(jwt).body

            // Get the issuer (user id) from the claims
            val userid = claims.issuer

            // If the userid is not present, return an error
            if (userid == null) {
                val httpResponse = response as HttpServletResponse
                httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
                httpResponse.writer.write("Unauthorized: invalid JWT")
                return
            }

            val userDetails =
                userRepository.findById(UUID.fromString(userid)).orElseThrow {
                    throw CustomAuthException(HttpStatus.BAD_REQUEST, "User not found")
                }
            val usernamePasswordAuthenticationToken =
                UsernamePasswordAuthenticationToken(userDetails, null, null)
            usernamePasswordAuthenticationToken.details =
                WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken

            // If the JWT is valid, pass the request to the next filter in the chain or to the final
            // destination
            chain.doFilter(request, response)
        } catch (e: Exception) {
            // If there is any error parsing or validating the JWT, return an error
            val httpResponse = response as HttpServletResponse
            httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
            httpResponse.writer.write("Unauthorized: invalid JWT")
        }
    }
}
