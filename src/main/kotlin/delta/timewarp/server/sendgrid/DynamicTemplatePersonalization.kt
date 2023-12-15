package delta.timewarp.server.sendgrid

import com.fasterxml.jackson.annotation.JsonProperty
import com.sendgrid.Personalization
import java.util.Collections
import kotlin.collections.HashMap

class DynamicTemplatePersonalization : Personalization() {
    @JsonProperty(value = "dynamic_template_data")
    private var dynamicTemplateData: HashMap<String, String>? = null

    @JsonProperty(value = "dynamic_template_data")
    fun getDynamicTemplateData(): Map<String, String> {
        return dynamicTemplateData ?: (Collections.emptyMap())
    }

    fun addDynamicTemplateData(key: String, value: String) {
        dynamicTemplateData = dynamicTemplateData ?: HashMap()
        dynamicTemplateData!![key] = value
    }
}
