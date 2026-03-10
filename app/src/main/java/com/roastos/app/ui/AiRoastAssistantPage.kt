package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.EnergySnapshot
import com.roastos.app.MachineProfiles
import com.roastos.app.MachineStateEngine
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.RoastCompanionLayer
import com.roastos.app.RoastCompanionMode
import com.roastos.app.RoastCompanionPresence
import com.roastos.app.RoastCompanionState
import com.roastos.app.RoastInsightEngine
import com.roastos.app.RoastStabilityResult
import com.roastos.app.UiKit

object AiRoastAssistantPage {

    private var companionState =
        RoastCompanionState.initial(
            mode = RoastCompanionMode.QUIET
        )

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(
            UiKit.pageTitle(
                context,
                "ROAST COMPANION"
            )
        )

        root.addView(
            UiKit.pageSubtitle(
                context,
                "Quiet observation, clear insight, possible directions"
            )
        )

        root.addView(UiKit.spacer(context))

        val quietModeBtn = Button(context)
        quietModeBtn.text = "Quiet"

        val supportiveModeBtn = Button(context)
        supportiveModeBtn.text = "Supportive"

        val explorationModeBtn = Button(context)
        explorationModeBtn.text = "Exploration"

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh"

        val askCompanionBtn = Button(context)
        askCompanionBtn.text = "Ask Companion"

        val exploreBtn = Button(context)
        exploreBtn.text = "Explore"

        root.addView(quietModeBtn)
        root.addView(supportiveModeBtn)
        root.addView(explorationModeBtn)
        root.addView(refreshBtn)
        root.addView(askCompanionBtn)
        root.addView(exploreBtn)

        root.addView(UiKit.spacer(context))

        val stateCard = UiKit.card(context)
        stateCard.addView(
            UiKit.cardTitle(
                context,
                "SYSTEM STATE"
            )
        )
        val stateBody = UiKit.bodyText(context, "")
        stateCard.addView(stateBody)
        root.addView(stateCard)

        root.addView(UiKit.spacer(context))

        val companionCard = UiKit.card(context)
        companionCard.addView(
            UiKit.cardTitle(
                context,
                "COMPANION"
            )
        )
        val companionBody = UiKit.bodyText(context, "")
        companionCard.addView(companionBody)
        root.addView(companionCard)

        root.addView(UiKit.spacer(context))

        val observationCard = UiKit.card(context)
        observationCard.addView(
            UiKit.cardTitle(
                context,
                "OBSERVATIONS"
            )
        )
        val observationBody = UiKit.bodyText(context, "")
        observationCard.addView(observationBody)
        root.addView(observationCard)

        root.addView(UiKit.spacer(context))

        val possibilityCard = UiKit.card(context)
        possibilityCard.addView(
            UiKit.cardTitle(
                context,
                "POSSIBLE DIRECTIONS"
            )
        )
        val possibilityBody = UiKit.bodyText(context, "")
        possibilityCard.addView(possibilityBody)
        root.addView(possibilityCard)

        fun render(
            userRequested: Boolean = false,
            explorationRequested: Boolean = false
        ) {
            val profile = MachineProfiles.HB_M2SE
            val telemetry = MachineTelemetryEngine.currentState()

            val machineState = MachineStateEngine.buildState(
                powerW = telemetry.livePowerW,
                airflowPa = telemetry.liveAirflowPa,
                drumRpm = telemetry.liveDrumRpm,
                beanTemp = telemetry.liveBtC ?: 0.0,
                ror = telemetry.liveRorCPerMin ?: 0.0,
                elapsedSec = telemetry.liveElapsedSec,
                environmentTemp = 25.0,
                environmentHumidity = 50.0
            )

            val energy: EnergySnapshot? = null
            val stability: RoastStabilityResult? = null

            val report = RoastInsightEngine.analyze(
                profile = profile,
                machineState = machineState,
                energy = energy,
                stability = stability,
                styleGoal = null
            )

            val decision = RoastCompanionLayer.evaluate(
                currentState = companionState,
                report = report,
                machineState = machineState,
                userRequested = userRequested,
                explorationRequested = explorationRequested
            )

            companionState = decision.nextState

            stateBody.text = buildString {
                append("Mode\n")
                append(companionState.mode)
                append("\n\n")
                append("Presence\n")
                append(decision.presence)
                append("\n\n")
                append("Quiet Summary\n")
                append(report.quietSummary)
                append("\n\n")
                append("Telemetry\n")
                append(MachineTelemetryEngine.summary())
            }

            companionBody.text = when (decision.presence) {
                RoastCompanionPresence.SILENT ->
                    "System quiet."

                else ->
                    RoastCompanionLayer.renderForUi(decision)
            }

            observationBody.text =
                if (report.observations.isEmpty()) {
                    "-"
                } else {
                    report.observations.joinToString("\n\n") { it.summary() }
                }

            possibilityBody.text =
                if (report.possibilities.isEmpty()) {
                    "-"
                } else {
                    report.possibilities.joinToString("\n\n") { it.summary() }
                }
        }

        quietModeBtn.setOnClickListener {
            companionState = companionState.copy(
                mode = RoastCompanionMode.QUIET
            )
            render()
        }

        supportiveModeBtn.setOnClickListener {
            companionState = companionState.copy(
                mode = RoastCompanionMode.SUPPORTIVE
            )
            render()
        }

        explorationModeBtn.setOnClickListener {
            companionState = companionState.copy(
                mode = RoastCompanionMode.EXPLORATION
            )
            render()
        }

        refreshBtn.setOnClickListener {
            render()
        }

        askCompanionBtn.setOnClickListener {
            render(userRequested = true)
        }

        exploreBtn.setOnClickListener {
            render(explorationRequested = true)
        }

        render()

        scroll.addView(root)
        container.addView(scroll)
    }
}
