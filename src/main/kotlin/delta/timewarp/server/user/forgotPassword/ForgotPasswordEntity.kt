package delta.timewarp.server.user.forgotPassword

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.Date
import java.util.UUID

@Document(collection = "forgot_password_user")
data class ForgotPasswordEntity(
    @Id val id: UUID,
    val userId: UUID,
    val email: String,
    var token: String,
    var expiration: Date
)
