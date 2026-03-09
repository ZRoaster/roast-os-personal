package com.roastos.app

enum class RoastAiProviderType {
    MOCK,
    REMOTE_API
}

data class RoastAiServiceConfig(
    val providerType: RoastAiProviderType = RoastAiProviderType.MOCK,
    val modelName: String = "default",
    val apiBaseUrl: String = "",
    val apiKeyHint: String = "",
    val enableVision: Boolean = false,
    val enableAudio: Boolean = false,
    val enableFileContext: Boolean = false
) {
    fun summary(): String {
        return """
Roast AI Service Config

Provider
$providerType

Model
$modelName

Api Base Url
${if (apiBaseUrl.isBlank()) "-" else apiBaseUrl}

Vision Enabled
${if (enableVision) "Yes" else "No"}

Audio Enabled
${if (enableAudio) "Yes" else "No"}

File Context Enabled
${if (enableFileContext) "Yes" else "No"}
        """.trimIndent()
    }
}

data class RoastAiRequest(
    val context: RoastAiContext,
    val systemInstruction: String = "",
    val userMessageOverride: String = ""
) {
    fun summary(): String {
        return """
Roast AI Request

Intent
${context.intent}

System Instruction
${if (systemInstruction.isBlank()) "-" else systemInstruction}

User Message Override
${if (userMessageOverride.isBlank()) "-" else userMessageOverride}
        """.trimIndent()
    }
}

data class RoastAiServiceResult(
    val success: Boolean,
    val providerType: RoastAiProviderType,
    val response: RoastAiResponse?,
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

interface RoastAiProvider {

    val type: RoastAiProviderType

    fun generate(
        request: RoastAiRequest,
        config: RoastAiServiceConfig
    ): RoastAiServiceResult
}

class MockRoastAiProvider : RoastAiProvider {

    override val type: RoastAiProviderType = RoastAiProviderType.MOCK

    override fun generate(
        request: RoastAiRequest,
        config: RoastAiServiceConfig
    ): RoastAiServiceResult {

        val context = request.context

        val response = when (context.intent) {
            RoastAiIntentType.REALTIME_COACHING -> buildRealtimeCoachingResponse(context)
            RoastAiIntentType.DIAGNOSIS -> buildDiagnosisResponse(context)
            RoastAiIntentType.STYLE_GENERATION -> buildStyleResponse(context)
            RoastAiIntentType.BREW_RECOMMENDATION -> buildBrewResponse(context)
            RoastAiIntentType.ROAST_PLANNING -> buildPlanningResponse(context)
            RoastAiIntentType.CONTROL_REVIEW -> buildControlReviewResponse(context)
            RoastAiIntentType.EXECUTION_REVIEW -> buildExecutionReviewResponse(context)
            RoastAiIntentType.POST_ROAST_REVIEW -> buildPostRoastReviewResponse(context)
            RoastAiIntentType.GENERAL_ASSIST -> RoastAiResponses.textExplanation(
                text = "Roast AI mock provider is ready. Provide more roast context for a stronger answer.",
                reasoning = "Fallback general-assist response"
            )
        }

        return RoastAiServiceResult(
            success = true,
            providerType = type,
            response = response,
            errorMessage = "",
            rawPayload = context.compactPromptBlock()
        )
    }

    private fun buildRealtimeCoachingResponse(
        context: RoastAiContext
    ): RoastAiResponse {

        val energy = context.energySnapshot
        val stability = context.stabilityResult
        val driving = context.drivingAdvice

        val explanation = buildString {
            append("Real-time coaching based on current roast state. ")
            if (energy != null) {
                append("Energy is ${energy.energyState.lowercase()}. ")
            }
            if (stability != null) {
                append("Stability is ${stability.stability.lowercase()}. ")
            }
            if (driving != null) {
                append("Suggested action level is ${driving.actionLevel.lowercase()}.")
            }
        }.trim()

        val controlSuggestion = when {
            driving?.heatAdvice?.contains("Increase", ignoreCase = true) == true ->
                RoastAiControlSuggestion(
                    heatDeltaW = 60,
                    airflowDeltaPa = null,
                    drumDeltaRpm = null,
                    reason = driving.reason
                )

            driving?.heatAdvice?.contains("Reduce", ignoreCase = true) == true ->
                RoastAiControlSuggestion(
                    heatDeltaW = -60,
                    airflowDeltaPa = null,
                    drumDeltaRpm = null,
                    reason = driving.reason
                )

            driving?.airflowAdvice?.contains("Increase", ignoreCase = true) == true ->
                RoastAiControlSuggestion(
                    heatDeltaW = null,
                    airflowDeltaPa = 2,
                    drumDeltaRpm = null,
                    reason = driving.reason
                )

            driving?.airflowAdvice?.contains("Reduce", ignoreCase = true) == true ->
                RoastAiControlSuggestion(
                    heatDeltaW = null,
                    airflowDeltaPa = -2,
                    drumDeltaRpm = null,
                    reason = driving.reason
                )

            else -> null
        }

        return RoastAiResponses.roastAdvice(
            explanation = if (explanation.isBlank()) {
                "Maintain close observation and apply smaller earlier corrections."
            } else {
                explanation
            },
            controlSuggestion = controlSuggestion,
            confidence = when {
                stability?.confidence == "High" -> RoastAiConfidenceLevel.HIGH
                stability?.confidence == "Medium" -> RoastAiConfidenceLevel.MEDIUM
                else -> RoastAiConfidenceLevel.MEDIUM
            },
            reasoning = "Mock provider derived response from energy, stability, and driving advice."
        )
    }

    private fun buildDiagnosisResponse(
        context: RoastAiContext
    ): RoastAiResponse {

        val explanation = buildString {
            append("Diagnosis summary. ")
            context.stabilityResult?.let {
                append("Roast stability is ${it.stability.lowercase()}. ")
            }
            context.energySnapshot?.let {
                append("Energy state is ${it.energyState.lowercase()}. ")
            }
            context.decisionResult?.let {
                append("Current decision path is '${it.suggestion}'.")
            }
        }.trim()

        return RoastAiResponses.diagnosis(
            explanation = if (explanation.isBlank()) {
                "Not enough roast data for a stronger diagnosis yet."
            } else {
                explanation
            },
            confidence = RoastAiConfidenceLevel.MEDIUM,
            reasoning = "Mock diagnosis built from available Roast OS engine outputs."
        )
    }

    private fun buildStyleResponse(
        context: RoastAiContext
    ): RoastAiResponse {

        val goal = context.styleGoal

        val proposal = RoastAiStyleProposal(
            styleName = goal?.styleName ?: "Balanced House Style",
            description = buildString {
                append(goal?.flavorDirection ?: "Balanced sweetness and clarity")
                append(" under current machine and control constraints.")
            },
            targetDevelopmentPercent = when (goal?.developmentPreference?.lowercase()) {
                "short" -> 14.0
                "medium" -> 16.5
                "long" -> 18.5
                else -> 16.0
            },
            energyStrategy = "Use earlier controlled energy rather than late aggressive pushes.",
            airflowStrategy = "Expand airflow gradually to protect clarity without stripping too much heat."
        )

        return RoastAiResponses.styleProposal(
            proposal = proposal,
            explanation = "Generated a style proposal from the requested flavor direction and development preference.",
            reasoning = "Mock style planner using RoastAiStyleGoal."
        )
    }

    private fun buildBrewResponse(
        context: RoastAiContext
    ): RoastAiResponse {

        val brew = RoastAiBrewRecommendation(
            brewMethod = "Pour Over",
            ratio = "1:15.5",
            grindLevel = "Medium",
            waterTempC = 92.0,
            notes = "Adjust slightly finer for more sweetness or slightly coarser for more clarity."
        )

        return RoastAiResponses.brewRecommendation(
            brew = brew,
            explanation = "Generated a conservative brew recommendation based on current roast context.",
            reasoning = "Mock brew recommendation with safe default parameters."
        )
    }

    private fun buildPlanningResponse(
        context: RoastAiContext
    ): RoastAiResponse {
        return RoastAiResponses.textExplanation(
            text = "Planning context received. Use bean data, machine profile, and style goal to draft the next roast plan.",
            reasoning = "Mock planning response"
        )
    }

    private fun buildControlReviewResponse(
        context: RoastAiContext
    ): RoastAiResponse {
        return RoastAiResponses.textExplanation(
            text = "Control review complete. Existing control plan can be inspected before any machine execution.",
            reasoning = "Mock control review response"
        )
    }

    private fun buildExecutionReviewResponse(
        context: RoastAiContext
    ): RoastAiResponse {
        return RoastAiResponses.textExplanation(
            text = "Execution review complete. Inspect command results, confirmation requirements, and blocked operations.",
            reasoning = "Mock execution review response"
        )
    }

    private fun buildPostRoastReviewResponse(
        context: RoastAiContext
    ): RoastAiResponse {
        return RoastAiResponses.textExplanation(
            text = "Post-roast review prepared. Compare stability, energy, and operator intent against the final result.",
            reasoning = "Mock post-roast review response"
        )
    }
}

class RemoteRoastAiProvider : RoastAiProvider {

    override val type: RoastAiProviderType = RoastAiProviderType.REMOTE_API

    override fun generate(
        request: RoastAiRequest,
        config: RoastAiServiceConfig
    ): RoastAiServiceResult {

        return RoastAiServiceResult(
            success = false,
            providerType = type,
            response = null,
            errorMessage = "Remote AI provider is not implemented yet. Wire your API transport here.",
            rawPayload = request.context.compactPromptBlock()
        )
    }
}

object RoastAiService {

    private var config = RoastAiServiceConfig()
    private var provider: RoastAiProvider = MockRoastAiProvider()

    fun currentConfig(): RoastAiServiceConfig = config

    fun providerType(): RoastAiProviderType = provider.type

    fun configure(newConfig: RoastAiServiceConfig) {
        config = newConfig
        provider = when (newConfig.providerType) {
            RoastAiProviderType.MOCK -> MockRoastAiProvider()
            RoastAiProviderType.REMOTE_API -> RemoteRoastAiProvider()
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
            systemInstruction = "You are a roast diagnosis assistant. Explain risks, deviations, and likely causes."
        )
    }

    fun generateStyleProposal(
        context: RoastAiContext
    ): RoastAiServiceResult {
        return generate(
            context = context,
            systemInstruction = "You are a roast style planner. Propose styles grounded in machine capability and roast physics."
        )
    }

    fun generateBrewRecommendation(
        context: RoastAiContext
    ): RoastAiServiceResult {
        return generate(
            context = context,
            systemInstruction = "You are a coffee brewing advisor. Produce grounded brew recommendations from roast context."
        )
    }

    fun summary(): String {
        return """
Roast AI Service

Provider
${provider.type}

Config
${config.summary()}
        """.trimIndent()
    }
}
