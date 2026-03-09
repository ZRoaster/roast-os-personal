package com.roastos.app

data class RoastAiServiceResult(

    val success: Boolean,

    val providerType: RoastAiProviderType,

    val response: RoastAiResponse? = null,

    val errorMessage: String = "",

    val rawPayload: String = ""

) {

    fun summary(): String {

        return """
Roast AI Service Result

Success
${if (success) "Yes" else "No"}

Provider
$providerType

Response Type
${response?.responseType ?: "-"}

Error
${if (errorMessage.isBlank()) "-" else errorMessage}
        """.trimIndent()
    }
}
