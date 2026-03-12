package com.roastos.app

object RoastAiRealtimeContextBuilder {

    fun build(
        intent: RoastAiIntentType = RoastAiIntentType.REALTIME_COACHING,
        userPrompt: String = "",
        operatorNote: String = ""
    ): RoastAiContext {

        val environmentProfile = EnvironmentProfileEngine.current()
        val environmentCompensation = EnvironmentCompensationEngine.evaluate()

        return RoastAiContext(
            sessionId = "realtime_" + System.currentTimeMillis(),
            intent = intent,

            machineProfile = null,
            machineState = null,
            telemetryFrame = null,
            controlCapability = null,

            energySnapshot = null,
            stabilityResult = null,
            drivingAdvice = null,
            decisionResult = null,

            controlPlan = null,
            executionSummary = null,

            styleGoal = null,

            environmentProfile = environmentProfile,
            environmentCompensation = environmentCompensation,

            userPrompt = userPrompt,
            operatorNote = operatorNote,

            attachments = emptyList()
        )
    }
}
