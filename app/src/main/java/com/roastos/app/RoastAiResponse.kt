package com.roastos.app

enum class RoastAiResponseType {
    TEXT_EXPLANATION,
    ROAST_ADVICE,
    CONTROL_SUGGESTION,
    STYLE_PROPOSAL,
    DIAGNOSIS,
    BREW_RECOMMENDATION
}

enum class RoastAiConfidenceLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class RoastAiControlSuggestion(
    val heatDeltaW: Int? = null,
    val airflowDeltaPa: Int? = null,
    val drumDeltaRpm: Int? = null,
    val reason: String = ""
) {
    fun summary(): String {

        return """
Control Suggestion

Heat Delta
${heatDeltaW ?: "-"}

Airflow Delta
${airflowDeltaPa ?: "-"}

Drum Delta
${drumDeltaRpm ?: "-"}

Reason
${if (reason.isBlank()) "-" else reason}
        """.trimIndent()
    }
}

data class RoastAiStyleProposal(
    val styleName: String,
    val description: String,
    val targetDevelopmentPercent: Double? = null,
    val energyStrategy: String = "",
    val airflowStrategy: String = ""
) {
    fun summary(): String {

        return """
Style Proposal

Name
$styleName

Description
$description

Target Development
${targetDevelopmentPercent ?: "-"} %

Energy Strategy
${if (energyStrategy.isBlank()) "-" else energyStrategy}

Airflow Strategy
${if (airflowStrategy.isBlank()) "-" else airflowStrategy}
        """.trimIndent()
    }
}

data class RoastAiBrewRecommendation(
    val brewMethod: String,
    val ratio: String,
    val grindLevel: String,
    val waterTempC: Double?,
    val notes: String = ""
) {

    fun summary(): String {

        return """
Brew Recommendation

Method
$brewMethod

Ratio
$ratio

Grind
$grindLevel

Water Temp
${waterTempC ?: "-"}

Notes
${if (notes.isBlank()) "-" else notes}
        """.trimIndent()
    }
}

data class RoastAiResponse(

    val responseType: RoastAiResponseType,

    val explanation: String = "",

    val controlSuggestion: RoastAiControlSuggestion? = null,

    val styleProposal: RoastAiStyleProposal? = null,

    val brewRecommendation: RoastAiBrewRecommendation? = null,

    val confidence: RoastAiConfidenceLevel = RoastAiConfidenceLevel.MEDIUM,

    val reasoning: String = "",

    val rawText: String = "",

    val createdAtMillis: Long = System.currentTimeMillis()
) {

    fun summary(): String {

        val controlText = controlSuggestion?.summary() ?: "-"
        val styleText = styleProposal?.summary() ?: "-"
        val brewText = brewRecommendation?.summary() ?: "-"

        return """
Roast AI Response

Type
$responseType

Explanation
${if (explanation.isBlank()) "-" else explanation}

Control Suggestion
$controlText

Style Proposal
$styleText

Brew Recommendation
$brewText

Confidence
$confidence

Reasoning
${if (reasoning.isBlank()) "-" else reasoning}
        """.trimIndent()
    }

    fun compact(): String {

        return """
AI

Type
$responseType

Confidence
$confidence

Explanation
${if (explanation.isBlank()) "-" else explanation}
        """.trimIndent()
    }
}

object RoastAiResponses {

    fun textExplanation(
        text: String,
        reasoning: String = ""
    ): RoastAiResponse {

        return RoastAiResponse(
            responseType = RoastAiResponseType.TEXT_EXPLANATION,
            explanation = text,
            reasoning = reasoning
        )
    }

    fun roastAdvice(
        explanation: String,
        controlSuggestion: RoastAiControlSuggestion?,
        confidence: RoastAiConfidenceLevel,
        reasoning: String
    ): RoastAiResponse {

        return RoastAiResponse(
            responseType = RoastAiResponseType.ROAST_ADVICE,
            explanation = explanation,
            controlSuggestion = controlSuggestion,
            confidence = confidence,
            reasoning = reasoning
        )
    }

    fun styleProposal(
        proposal: RoastAiStyleProposal,
        explanation: String,
        reasoning: String
    ): RoastAiResponse {

        return RoastAiResponse(
            responseType = RoastAiResponseType.STYLE_PROPOSAL,
            styleProposal = proposal,
            explanation = explanation,
            reasoning = reasoning
        )
    }

    fun diagnosis(
        explanation: String,
        confidence: RoastAiConfidenceLevel,
        reasoning: String
    ): RoastAiResponse {

        return RoastAiResponse(
            responseType = RoastAiResponseType.DIAGNOSIS,
            explanation = explanation,
            confidence = confidence,
            reasoning = reasoning
        )
    }

    fun brewRecommendation(
        brew: RoastAiBrewRecommendation,
        explanation: String,
        reasoning: String
    ): RoastAiResponse {

        return RoastAiResponse(
            responseType = RoastAiResponseType.BREW_RECOMMENDATION,
            brewRecommendation = brew,
            explanation = explanation,
            reasoning = reasoning
        )
    }
}
