package delta.timewarp.server.user.activateUser

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID
@Repository
interface ActivateUserRepository : MongoRepository<ActivateUserEntity, UUID> {

    fun findByToken(token: String): ActivateUserEntity? {
        return findByToken(token)
    }
    override fun findById(userId: UUID): Optional<ActivateUserEntity> {
        return findById(userId)
    }
}
