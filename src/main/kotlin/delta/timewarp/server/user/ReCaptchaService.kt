package delta.timewarp.server.user

import delta.timewarp.server.user.dtos.CaptchaDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class ReCaptchaService {

    @Value("\${recaptcha.keys.secret}")
    private lateinit var keySecret: String

    @Value("\${recaptcha.keys.threshold}")
    private lateinit var threshold: String

    @Value("\${recaptcha.keys.toggle}")
    private lateinit var recaptchaToggle: String

    fun validateCaptcha(token: String, ip: String): Boolean {
        if (!recaptchaToggle.toBoolean()) return true
        val restTemplate = RestTemplate()
        val verifyUri: URI =
            URI.create(
                String.format(
                    "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s",
                    keySecret,
                    token,
                    ip
                )
            )
        val googleResponse: CaptchaDTO? = restTemplate.getForObject(verifyUri, CaptchaDTO::class.java)
        return (googleResponse!!.success && googleResponse.score.toFloat() > threshold.toFloat())
    }
}
