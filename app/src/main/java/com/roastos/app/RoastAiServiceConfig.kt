package com.roastos.app

data class RoastAiServiceConfig(

    val providerType: RoastAiProviderType =
        RoastAiProviderType.MOCK,

    val modelName: String = "default",

    val apiBaseUrl: String = "",

    val apiKeyHint: String = "",

    val enableVision: Boolean = false,

    val enableAudio: Boolean = false,

    val enableFileContext: Boolean = false

) {

    fun summary(): String {

        return """
AI Service Config

Provider
$providerType

Model
$modelName

API Base
${if (apiBaseUrl.isBlank()) "Not Set" else apiBaseUrl}

API Key
${if (apiKeyHint.isBlank()) "Not Set" else "Configured"}

Vision
${if (enableVision) "Enabled" else "Disabled"}

Audio
${if (enableAudio) "Enabled" else "Disabled"}

File Context
${if (enableFileContext) "Enabled" else "Disabled"}
        """.trimIndent()
    }
}
