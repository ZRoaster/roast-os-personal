package com.roastos.app

enum class RoastAiInputModality {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO,
    FILE,
    SENSOR,
    UNKNOWN
}

enum class RoastAiIntentType {
    GENERAL_ASSIST,
    ROAST_PLANNING,
    REALTIME_COACHING,
    DIAGNOSIS,
    STYLE_GENERATION,
    CONTROL_REVIEW,
    EXECUTION_REVIEW,
    BREW_RECOMMENDATION,
    POST_ROAST_REVIEW
}

data class RoastAiAttachment(
    val id: String,
    val modality: RoastAiInputModality,
    val label: String,
    val contentHint: String,
    val uri: String? = null,
    val mimeType: String? = null
) {
    fun summary(): String {
        return """
Attachment
$id

Modality
$modality

Label
$label

Hint
$contentHint

Uri
${uri ?: "-"}

Mime
${mimeType ?: "-"}
        """.trimIndent()
    }
}

data class RoastAiStyleGoal(
    val styleName: String,
    val flavorDirection: String,
    val acidityPreference: String,
    val sweetnessPreference: String,
    val bodyPreference: String,
    val clarityPreference: String,
    val developmentPreference: String,
    val notes: String = ""
) {
    fun summary(): String {
        return """
Style Goal

Style
$styleName

Flavor Direction
$flavorDirection

Acidity
$acidityPreference

Sweetness
$sweetnessPreference

Body
$bodyPreference

Clarity
$clarityPreference

Development
$developmentPreference

Notes
${if (notes.isBlank()) "-" else notes}
        """.trimIndent()
    }
}

data class RoastAiContext(
    val sessionId: String,
    val intent: RoastAiIntentType,

    val machineProfile: MachineProfile? = null,
    val machineState: MachineState? = null,
    val telemetryFrame: MachineTelemetryFrame? = null,
    val controlCapability: MachineControlCapability? = null,

    val energySnapshot: EnergySnapshot? = null,
    val stabilityResult: RoastStabilityResult? = null,
    val drivingAdvice: RoastDrivingAdvice? = null,
    val decisionResult: DecisionEngine.DecisionResult? = null,

    val controlPlan: MachineControlPlan? = null,
    val executionSummary: MachineControlExecutionSummary? = null,

    val styleGoal: RoastAiStyleGoal? = null,

    val userPrompt: String = "",
    val operatorNote: String = "",

    val attachments: List<RoastAiAttachment> = emptyList(),

    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun summary(): String {
        return """
Roast AI Context

Session
$sessionId

Intent
$intent

Machine
${machineProfile?.name ?: "-"}

Telemetry
${telemetryFrame?.source ?: "-"}

Control Level
${controlCapability?.controlLevel ?: "-"}

Energy
${energySnapshot?.energyState ?: "-"}

Stability
${stabilityResult?.stability ?: "-"}

Driving Advice
${drivingAdvice?.actionLevel ?: "-"}

Decision
${decisionResult?.suggestion ?: "-"}

Primary Command
${controlPlan?.primaryCommand?.type ?: "-"}

Execution Status
${executionSummary?.overallStatus ?: "-"}

Style Goal
${styleGoal?.styleName ?: "-"}

User Prompt
${if (userPrompt.isBlank()) "-" else userPrompt}

Operator Note
${if (operatorNote.isBlank()) "-" else operatorNote}

Attachments
${attachments.size}
        """.trimIndent()
    }

    fun compactPromptBlock(): String {
        val attachmentText = if (attachments.isEmpty()) {
            "-"
        } else {
            attachments.joinToString(separator = "\n\n") { it.summary() }
        }

        return """
[Roast AI Context]

Intent
$intent

Machine Profile
${machineProfile?.name ?: "-"}

Machine State
${machineState?.let { machineStateBlock(it) } ?: "-"}

Telemetry
${telemetryFrame?.summary() ?: "-"}

Control Capability
${controlCapability?.summary() ?: "-"}

Energy
${energySnapshot?.summary ?: "-"}

Stability
${stabilityResult?.summary ?: "-"}

Driving Advice
${drivingAdvice?.summary ?: "-"}

Decision
${decisionResult?.let { decisionBlock(it) } ?: "-"}

Control Plan
${controlPlan?.summary ?: "-"}

Execution Summary
${executionSummary?.summary ?: "-"}

Style Goal
${styleGoal?.summary() ?: "-"}

User Prompt
${if (userPrompt.isBlank()) "-" else userPrompt}

Operator Note
${if (operatorNote.isBlank()) "-" else operatorNote}

Attachments
$attachmentText
        """.trimIndent()
    }

    private fun machineStateBlock(state: MachineState): String {
        return """
Power
${state.powerW}W

Airflow
${state.airflowPa}Pa

Drum
${state.drumRpm}rpm

Bean Temp
${"%.1f".format(state.beanTemp)}℃

ROR
${"%.1f".format(state.ror)}℃/min

Elapsed
${state.elapsedSec}s

Environment
${"%.1f".format(state.environmentTemp)}℃ / ${"%.0f".format(state.environmentHumidity)}%RH
        """.trimIndent()
    }

    private fun decisionBlock(result: DecisionEngine.DecisionResult): String {
        return """
Suggestion
${result.suggestion}

Severity
${result.severity}

Reason
${result.reason}
        """.trimIndent()
    }
}

object RoastAiContexts {

    fun buildRealtimeCoachingContext(
        machineProfile: MachineProfile,
        machineState: MachineState,
        telemetryFrame: MachineTelemetryFrame?,
        controlCapability: MachineControlCapability,
        energySnapshot: EnergySnapshot,
        stabilityResult: RoastStabilityResult?,
        drivingAdvice: RoastDrivingAdvice?,
        decisionResult: DecisionEngine.DecisionResult?,
        controlPlan: MachineControlPlan?,
        executionSummary: MachineControlExecutionSummary? = null,
        styleGoal: RoastAiStyleGoal? = null,
        userPrompt: String = "",
        operatorNote: String = "",
        attachments: List<RoastAiAttachment> = emptyList()
    ): RoastAiContext {
        return RoastAiContext(
            sessionId = buildSessionId("realtime"),
            intent = RoastAiIntentType.REALTIME_COACHING,
            machineProfile = machineProfile,
            machineState = machineState,
            telemetryFrame = telemetryFrame,
            controlCapability = controlCapability,
            energySnapshot = energySnapshot,
            stabilityResult = stabilityResult,
            drivingAdvice = drivingAdvice,
            decisionResult = decisionResult,
            controlPlan = controlPlan,
            executionSummary = executionSummary,
            styleGoal = styleGoal,
            userPrompt = userPrompt,
            operatorNote = operatorNote,
            attachments = attachments
        )
    }

    fun buildDiagnosisContext(
        machineProfile: MachineProfile?,
        machineState: MachineState?,
        telemetryFrame: MachineTelemetryFrame?,
        energySnapshot: EnergySnapshot?,
        stabilityResult: RoastStabilityResult?,
        decisionResult: DecisionEngine.DecisionResult?,
        userPrompt: String,
        attachments: List<RoastAiAttachment> = emptyList()
    ): RoastAiContext {
        return RoastAiContext(
            sessionId = buildSessionId("diagnosis"),
            intent = RoastAiIntentType.DIAGNOSIS,
            machineProfile = machineProfile,
            machineState = machineState,
            telemetryFrame = telemetryFrame,
            controlCapability = null,
            energySnapshot = energySnapshot,
            stabilityResult = stabilityResult,
            drivingAdvice = null,
            decisionResult = decisionResult,
            controlPlan = null,
            executionSummary = null,
            styleGoal = null,
            userPrompt = userPrompt,
            operatorNote = "",
            attachments = attachments
        )
    }

    fun buildStyleGenerationContext(
        machineProfile: MachineProfile?,
        controlCapability: MachineControlCapability?,
        styleGoal: RoastAiStyleGoal,
        userPrompt: String,
        attachments: List<RoastAiAttachment> = emptyList()
    ): RoastAiContext {
        return RoastAiContext(
            sessionId = buildSessionId("style"),
            intent = RoastAiIntentType.STYLE_GENERATION,
            machineProfile = machineProfile,
            machineState = null,
            telemetryFrame = null,
            controlCapability = controlCapability,
            energySnapshot = null,
            stabilityResult = null,
            drivingAdvice = null,
            decisionResult = null,
            controlPlan = null,
            executionSummary = null,
            styleGoal = styleGoal,
            userPrompt = userPrompt,
            operatorNote = "",
            attachments = attachments
        )
    }

    fun buildBrewRecommendationContext(
        machineProfile: MachineProfile?,
        styleGoal: RoastAiStyleGoal?,
        userPrompt: String,
        operatorNote: String = "",
        attachments: List<RoastAiAttachment> = emptyList()
    ): RoastAiContext {
        return RoastAiContext(
            sessionId = buildSessionId("brew"),
            intent = RoastAiIntentType.BREW_RECOMMENDATION,
            machineProfile = machineProfile,
            machineState = null,
            telemetryFrame = null,
            controlCapability = null,
            energySnapshot = null,
            stabilityResult = null,
            drivingAdvice = null,
            decisionResult = null,
            controlPlan = null,
            executionSummary = null,
            styleGoal = styleGoal,
            userPrompt = userPrompt,
            operatorNote = operatorNote,
            attachments = attachments
        )
    }

    fun buildMinimal(
        intent: RoastAiIntentType,
        userPrompt: String,
        attachments: List<RoastAiAttachment> = emptyList()
    ): RoastAiContext {
        return RoastAiContext(
            sessionId = buildSessionId("minimal"),
            intent = intent,
            userPrompt = userPrompt,
            attachments = attachments
        )
    }

    private fun buildSessionId(prefix: String): String {
        return prefix + "_" + System.currentTimeMillis()
    }
}
