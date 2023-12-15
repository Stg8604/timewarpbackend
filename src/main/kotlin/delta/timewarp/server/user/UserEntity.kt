package delta.timewarp.server.user

import delta.timewarp.server.user.enums.LoginType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID

@Document(collection = "user")
data class UserEntity(
    @Id val id: UUID,
    var username: String,
    @Indexed(unique = true) val email: String,
    val loginType: LoginType,
    var password: String,
    var isEnabled: Boolean = false
) {
    fun comparePassword(password: String): Boolean {
        return BCryptPasswordEncoder().matches(password, this.password)
    }
}
