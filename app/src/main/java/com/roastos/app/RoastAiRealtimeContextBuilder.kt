package com.roastos.app

object RoastAiRealtimeContextBuilder {

    fun build(
        intent: RoastAiIntentType = RoastAiIntentType.REALTIME_COACHING,
        userPrompt: String = "",
        operatorNote: String = ""
    ): RoastAiContext {

        val machineProfile = MachineProfileRegistry.currentOrNull()

        val machineState = MachineStateEngine.currentOrNull()

        val telemetry = MachineTelemetryEngine.latestOrNull()

        val capability = MachineControlCapabilityRegistry.currentOrNull()

        val energy = EnergyEngine.currentOrNull()

        val snapshot = RoastSessionBus.peek()

        val stability = try {
            if (snapshot != null) {

                if (snapshot.validation.hasIssues()) {
                    RoastStabilityResult(
                        stability = "watch",
                        summary = "Validation issues detected"
                    )
                } else {
                    RoastStabilityResult(
                        stability = "stable",
                        summary = "System stable"
                    )
                }

            } else null
        } catch (_: Throwable) {
            null
        }

        val driving = try {
            if (snapshot != null) {

                if (snapshot.validation.hasIssues()) {
                    RoastDrivingAdvice(
                        actionLevel = "adjust",
                        summary = "Adjustment suggested"
                    )
                } else {
                    RoastDrivingAdvice(
                        actionLevel = "hold",
                        summary = "Hold current trajectory"
                    )
                }

            } else null
        } catch (_: Throwable) {
            null
        }

        val decision = try {
            if (snapshot != null) {
                RoastDecisionEngine.evaluate(snapshot)
            } else null
        } catch (_: Throwable) {
            null
        }

        val environmentProfile = EnvironmentProfileEngine.current()

        val environmentCompensation = EnvironmentCompensationEngine.evaluate()

        return RoastAiContext(
            sessionId = "realtime_" + System.currentTimeMillis(),
            intent = intent,

            machineProfile = machineProfile,
            machineState = machineState,
            telemetryFrame = telemetry,
            controlCapability = capability,

            energySnapshot = energy,
            stabilityResult = stability,
            drivingAdvice = driving,
            decisionResult = decision,

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
