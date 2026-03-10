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

object RoastStudioPage {

    private var companionState =
        RoastCompanionState.initial(
            mode = RoastCompanionMode.QUIET
        )

    private var lastActionLabel: String =
        "No action yet."

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(
            UiKit.pageTitle(
                context,
                "ROAST STUDIO"
            )
        )

        root.addView(
            UiKit.pageSubtitle(
                context,
                "A quiet place to roast"
            )
        )

        root.addView(UiKit.spacer(context))

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh"

        val quietBtn = Button(context)
        quietBtn.text = "Quiet"

        val supportiveBtn = Button(context)
        supportiveBtn.text = "Supportive"

        val explorationBtn = Button(context)
        explorationBtn.text = "Exploration"

        val startRoastBtn = Button(context)
        startRoastBtn.text = "Start Roast"

        val openCompanionBtn = Button(context)
        openCompanionBtn.text = "Open Companion"

        val exploreBtn = Button(context)
        exploreBtn.text = "Explore"

        root.addView(refreshBtn)
        root.addView(quietBtn)
        root.addView(supportiveBtn)
        root.addView(explorationBtn)
        root.addView(startRoastBtn)
        root.addView(openCompanionBtn)
        root.addView(exploreBtn)

        root.addView(UiKit.spacer(context))

        val machineCard = UiKit.card(context)
        machineCard.addView(
            UiKit.cardTitle(
                context,
                "MACHINE"
            )
        )
        val machineBody = UiKit.bodyText(context, "")
        machineCard.addView(machineBody)
        root.addView(machineCard)

        root.addView(UiKit.spacer(context))

        val todayCard = UiKit.card(context)
        todayCard.addView(
            UiKit.cardTitle(
                context,
                "TODAY FOCUS"
            )
        )
        val todayBody = UiKit.bodyText(context, "")
        todayCard.addView(todayBody)
        root.addView(todayCard)

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

        val directionCard = UiKit.card(context)
        directionCard.addView(
            UiKit.cardTitle(
                context,
                "POSSIBLE DIRECTIONS"
            )
        )
        val directionBody = UiKit.bodyText(context, "")
        directionCard.addView(directionBody)
        root.addView(directionCard)

        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        actionCard.addView(
            UiKit.cardTitle(
                context,
                "WORKFLOW"
            )
        )
        val actionBody = UiKit.bodyText(context, "")
        actionCard.addView(actionBody)
        root.addView(actionCard)

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

            machineBody.text = buildString {
                append("Profile\n")
                append(profile.name)
                append("\n\n")
                append("Telemetry\n")
                append(MachineTelemetryEngine.summary())
            }

            todayBody.text = buildString {
                append("Mode\n")
                append(companionState.mode)
                append("\n\n")
                append("Presence\n")
                append(decision.presence)
                append("\n\n")
                append("Quiet Summary\n")
                append(report.quietSummary)
            }

            companionBody.text = when (decision.presence) {
                RoastCompanionPresence.SILENT ->
                    "System quiet."

                else ->
                    RoastCompanionLayer.renderForUi(decision)
            }

            directionBody.text =
                if (report.possibilities.isEmpty()) {
                    "-"
                } else {
                    report.possibilities.joinToString("\n\n") { possibility ->
                        possibility.summary()
                    }
                }

            actionBody.text = buildString {
                append("Last Action\n")
                append(lastActionLabel)
                append("\n\n")
                append("Suggested Next Step\n")
                append(suggestNextStep(report))
            }
        }

        refreshBtn.setOnClickListener {
            lastActionLabel = "Manual refresh."
            render()
        }

        quietBtn.setOnClickListener {
            companionState = companionState.copy(
                mode = RoastCompanionMode.QUIET
            )
            lastActionLabel = "Companion mode switched to Quiet."
            render()
        }

        supportiveBtn.setOnClickListener {
            companionState = companionState.copy(
                mode = RoastCompanionMode.SUPPORTIVE
            )
            lastActionLabel = "Companion mode switched to Supportive."
            render()
        }

        explorationBtn.setOnClickListener {
            companionState = companionState.copy(
                mode = RoastCompanionMode.EXPLORATION
            )
            lastActionLabel = "Companion mode switched to Exploration."
            render()
        }

        startRoastBtn.setOnClickListener {
            lastActionLabel = "Start Roast pressed."
            render(userRequested = true)
        }

        openCompanionBtn.setOnClickListener {
            lastActionLabel = "Open Companion pressed."
            render(userRequested = true)
        }

        exploreBtn.setOnClickListener {
            lastActionLabel = "Explore pressed."
            render(explorationRequested = true)
        }

        render()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun suggestNextStep(
        report: com.roastos.app.RoastInsightReport
    ): String {
        val primary = RoastInsightEngine.primaryInsight(report)

        return when {
            primary == null ->
                "Continue observing the roast calmly."

            primary.severity == com.roastos.app.RoastInsightSeverity.ALERT ->
                "Pause and check the current trend before making a strong adjustment."

            primary.severity == com.roastos.app.RoastInsightSeverity.WATCH ->
                "Stay attentive. A small adjustment may be enough if the trend continues."

            else ->
                "Maintain current rhythm and continue observing."
        }
    }
}
