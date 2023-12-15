package delta.timewarp.server.user.forgotPassword

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ForgotPasswordRepository : MongoRepository<ForgotPasswordEntity, UUID> {
    fun findByToken(token: String): ForgotPasswordEntity? {
        return findByToken(token)
    }
    fun findByEmail(email: String): ForgotPasswordEntity? {
        return findByEmail(email)
    }
}
