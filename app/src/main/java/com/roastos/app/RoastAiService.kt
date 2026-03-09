package com.roastos.app

object RoastAiService {

    private var config = RoastAiServiceConfig()

    private var provider: RoastAiProvider = MockRoastAiProvider()

    fun currentConfig(): RoastAiServiceConfig {
        return config
    }

    fun providerType(): RoastAiProviderType {
        return provider.type
    }

    fun configure(newConfig: RoastAiServiceConfig) {

        config = newConfig

        provider = when (newConfig.providerType) {

            RoastAiProviderType.MOCK -> {
                MockRoastAiProvider()
            }

            RoastAiProviderType.REMOTE_API -> {
                OpenAiProvider()
            }
        }
    }

    fun generate(
        context: RoastAiContext,
        systemInstruction: String = "",
        userMessageOverride: String = ""
    ): RoastAiServiceResult {

        val request = RoastAiRequest(
            context = context,
            systemInstruction = systemInstruction,
            userMessageOverride = userMessageOverride
        )

        return provider.generate(
            request = request,
            config = config
        )
    }

    fun generateRealtimeCoaching(
        context: RoastAiContext
    ): RoastAiServiceResult {

        return generate(
            context = context,
            systemInstruction = "You are a roast coaching assistant. Stay within machine limits and physical realism."
        )
    }

    fun generateDiagnosis(
        context: RoastAiContext
    ): RoastAiServiceResult {

        return generate(
            context = context,
            systemInstruction = "You are a roast diagnosis assistant. Explain roast deviations and risks."
        )
    }

    fun generateStyleProposal(
        context: RoastAiContext
    ): RoastAiServiceResult {

        return generate(
            context = context,
            systemInstruction = "You are a roast style planner. Generate roasting style proposals."
        )
    }

    fun generateBrewRecommendation(
        context: RoastAiContext
    ): RoastAiServiceResult {

        return generate(
            context = context,
            systemInstruction = "You are a coffee brewing advisor."
        )
    }

    fun summary(): String {

        return """
Roast AI Service

Provider
${provider.type}

Model
${config.modelName}

Base URL
${config.apiBaseUrl}

Vision
${if (config.enableVision) "Enabled" else "Disabled"}

Audio
${if (config.enableAudio) "Enabled" else "Disabled"}
        """.trimIndent()
    }
}
