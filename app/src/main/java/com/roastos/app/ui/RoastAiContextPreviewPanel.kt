package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.*

class RoastAiContextPreviewPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {

        val snapshot = RoastSessionBus.peek()

        if (snapshot == null) {

            textView.text = """
AI Context Preview

No active roast session.
            """.trimIndent()

            return
        }

        val machineProfile = MachineProfileRegistry.currentOrNull()
        val machineState = MachineStateEngine.currentOrNull()
        val telemetryFrame = MachineTelemetryEngine.latestOrNull()
        val controlCapability = MachineControlCapabilityRegistry.currentOrNull()
        val energySnapshot = EnergyEngine.currentOrNull()

        val stability = tryBuildStability(snapshot)
        val driving = tryBuildDriving(snapshot)
        val decision = tryBuildDecision(snapshot)

        val context = RoastAiContexts.buildMinimal(
            intent = RoastAiIntentType.REALTIME_COACHING,
            userPrompt = "Context preview",
            environmentProfile = EnvironmentProfileEngine.current(),
            environmentCompensation = EnvironmentCompensationEngine.evaluate()
        ).copy(
            machineProfile = machineProfile,
            machineState = machineState,
            telemetryFrame = telemetryFrame,
            controlCapability = controlCapability,
            energySnapshot = energySnapshot,
            stabilityResult = stability,
            drivingAdvice = driving,
            decisionResult = decision
        )

        textView.text = context.summary()
    }

    private fun tryBuildDecision(
        snapshot: RoastSessionBusSnapshot
    ): DecisionEngine.DecisionResult? {

        return try {

            val result = RoastDecisionEngine.evaluate(snapshot)

            DecisionEngine.DecisionResult(
                suggestion = result.heatAction,
                severity = result.confidence,
                reason = result.rationale
            )

        } catch (_: Throwable) {
            null
        }
    }

    private fun tryBuildStability(
        snapshot: RoastSessionBusSnapshot
    ): RoastStabilityResult? {

        return try {

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

        } catch (_: Throwable) {
            null
        }
    }

    private fun tryBuildDriving(
        snapshot: RoastSessionBusSnapshot
    ): RoastDrivingAdvice? {

        return try {

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

        } catch (_: Throwable) {
            null
        }
    }
}
