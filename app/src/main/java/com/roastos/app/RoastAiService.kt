package com.roastos.app

object RoastAiService {

    suspend fun generate(
        request: RoastAiRequest
    ): RoastAiResponse {

        val provider: RoastAiProvider = when (request.provider) {
            RoastAiProviderType.MOCK -> {
                MockRoastAiProvider()
            }

            RoastAiProviderType.REMOTE_API -> {
                MockRoastAiProvider()
            }
        }

        return provider.generate(request)
    }

    suspend fun generateRealtimeCoaching(
        context: RoastAiContext,
        providerType: RoastAiProviderType = RoastAiProviderType.MOCK,
        apiKey: String? = null,
        model: String? = null
    ): RoastAiResponse {

        val request = RoastAiRequest(
            provider = providerType,
            apiKey = apiKey,
            model = model,
            context = context,
            systemInstruction = "You are a roast coaching assistant. Stay within machine limits and physical realism."
        )

        return generate(request)
    }

    suspend fun generateDiagnosis(
        context: RoastAiContext,
        providerType: RoastAiProviderType = RoastAiProviderType.MOCK,
        apiKey: String? = null,
        model: String? = null
    ): RoastAiResponse {

        val request = RoastAiRequest(
            provider = providerType,
            apiKey = apiKey,
            model = model,
            context = context,
            systemInstruction = "You are a roast diagnosis assistant. Explain roast deviations and risks."
        )

        return generate(request)
    }

    suspend fun generateStyleProposal(
        context: RoastAiContext,
        providerType: RoastAiProviderType = RoastAiProviderType.MOCK,
        apiKey: String? = null,
        model: String? = null
    ): RoastAiResponse {

        val request = RoastAiRequest(
            provider = providerType,
            apiKey = apiKey,
            model = model,
            context = context,
            systemInstruction = "You are a roast style planner. Generate roasting style proposals."
        )

        return generate(request)
    }

    suspend fun generateBrewRecommendation(
        context: RoastAiContext,
        providerType: RoastAiProviderType = RoastAiProviderType.MOCK,
        apiKey: String? = null,
        model: String? = null
    ): RoastAiResponse {

        val request = RoastAiRequest(
            provider = providerType,
            apiKey = apiKey,
            model = model,
            context = context,
            systemInstruction = "You are a coffee brewing advisor."
        )

        return generate(request)
    }

    fun summary(): String {
        return """
Roast AI Service

Supported Providers
- MOCK
- REMOTE_API

Current Note
REMOTE_API is temporarily routed to MockRoastAiProvider until real provider wiring is finalized.
        """.trimIndent()
    }
}
