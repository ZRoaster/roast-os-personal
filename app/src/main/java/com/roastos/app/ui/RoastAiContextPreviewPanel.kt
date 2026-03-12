package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.*

class RoastAiContextPreviewPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = TextView(context)

    init {

        orientation = VERTICAL

        textView.textSize = 12f
        addView(textView)

        update()
    }

    fun update() {

        val snapshot = RoastSessionBus.peek()

        if (snapshot == null) {

            textView.text =
                """
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

        val contextPreview = RoastAiContexts.buildMinimal(
            intent = RoastAiIntentType.REALTIME_COACHING,
            userPrompt = "Preview current AI context",
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

        textView.text = contextPreview.summary()
    }

    private fun tryBuildDecision(
        snapshot: RoastSessionBusSnapshot
    ): DecisionEngine.DecisionResult? {

        return try {
            // 直接使用系统现有决策结果
            RoastDecisionEngine.evaluate(snapshot)
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
