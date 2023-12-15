package delta.timewarp.server.user.oauth2

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User = DefaultOAuth2UserService().loadUser(userRequest)
        val attributes = mutableMapOf<String, Any>()
        attributes.putAll(oAuth2User.attributes)
        return DefaultOAuth2User(oAuth2User.authorities, attributes, "id")
    }
}
