package delta.timewarp.server.sendgrid

import com.sendgrid.Email
import com.sendgrid.Mail
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*

@Service
class SendGridService {
    private val logger: Logger = LoggerFactory.getLogger(SendGridService::class.java)

    @Autowired private lateinit var sendGrid: SendGrid

    @Value("\${spring.sendgrid.email-verification-template-id}")
    private lateinit var activateUserTemplateId: String

    @Value("\${spring.sendgrid.forgot-password-template-id}")
    private lateinit var forgotPasswordTemplateId: String

    @Value("\${spring.sendgrid.sender-email}")
    private lateinit var senderEmail: String

    @Value("\${frontend-domain}")
    private lateinit var frontendDomainUrl: String

    @Value("\${production}")
    private lateinit var production: String

    fun activateUserEmail(userId: UUID, token: String, name: String, email: String) {
        if (!production.toBoolean()) return
        val link = "$frontendDomainUrl/activate/?userid=$userId&token=$token"
        val emailTo = email
        sendTemplateEmail(emailTo, name, link, activateUserTemplateId)
    }

    fun forgotUserEmail(token: String, email: String) {
        if (!production.toBoolean()) return
        val link = "$frontendDomainUrl/reset-password/?token=$token"
        val emailTo = email
        val name = ""
        sendTemplateEmail(emailTo, name, link, forgotPasswordTemplateId)
    }

    fun sendTemplateEmail(emailTo: String, name: String, link: String, templateId: String) {
        val mail = Mail()
        // the email is from
        mail.setFrom(Email(senderEmail))
        val personalization = DynamicTemplatePersonalization()
        personalization.addDynamicTemplateData("name", name)
        personalization.addDynamicTemplateData("link", link)
        // the email is to
        personalization.addTo(Email(emailTo))
        mail.addPersonalization(personalization)
        mail.setTemplateId(templateId)

        val request = Request()
        try {
            request.apply {
                method = Method.POST

                endpoint = "mail/send"

                body = mail.build()
            }
            val response: Response = sendGrid.api(request)

            if (response.statusCode >= 400) {
                logger.error(
                    "Error while sending email with status: ${response.statusCode}. Error: ${response.body}"
                )
                throw delta.timewarp.server.exception.CustomException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error"
                )
            }
        } catch (e: Exception) {
            logger.error("Error while sending email: ${e.message}")
            throw delta.timewarp.server.exception.CustomException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error."
            )
        }
    }
}
