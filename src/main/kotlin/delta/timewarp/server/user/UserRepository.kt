package delta.timewarp.server.user

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface UserRepository : MongoRepository<UserEntity, UUID> {

    fun findByEmail(email: String): UserEntity? {
        return findByEmail(email)
    }
    override fun findById(id: UUID): Optional<UserEntity> {
        return findById(id)
    }
}
