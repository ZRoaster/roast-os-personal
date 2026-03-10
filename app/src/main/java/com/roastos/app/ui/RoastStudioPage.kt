package com.roastos.app.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.EnergySnapshot
import com.roastos.app.MachineBridge
import com.roastos.app.MachineProfiles
import com.roastos.app.MachineStateEngine
import com.roastos.app.RoastCompanionLayer
import com.roastos.app.RoastCompanionMode
import com.roastos.app.RoastCompanionPresence
import com.roastos.app.RoastCompanionState
import com.roastos.app.RoastInsightEngine
import com.roastos.app.RoastSessionEngine
import com.roastos.app.RoastStabilityResult
import com.roastos.app.UiKit

object RoastStudioPage {

    private var companionState =
        RoastCompanionState.initial(
            mode = RoastCompanionMode.QUIET
        )

    private var lastActionLabel: String =
        "No action yet."

    private val uiHandler = Handler(Looper.getMainLooper())
    private var autoRefreshRunnable: Runnable? = null

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()
        stopAutoRefresh()

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
                "Quiet system, clear observation, live roast session"
            )
        )

        root.addView(UiKit.spacer(context))

        val primaryActionsCard = UiKit.card(context)
        primaryActionsCard.addView(
            UiKit.cardTitle(
                context,
                "PRIMARY ACTIONS"
            )
        )

        val startRoastBtn = Button(context)
        startRoastBtn.text = "Start Roast"

        val stopRoastBtn = Button(context)
        stopRoastBtn.text = "Stop Roast"

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh"

        val askCompanionBtn = Button(context)
        askCompanionBtn.text = "Ask Companion"

        primaryActionsCard.addView(startRoastBtn)
        primaryActionsCard.addView(stopRoastBtn)
        primaryActionsCard.addView(refreshBtn)
        primaryActionsCard.addView(askCompanionBtn)

        root.addView(primaryActionsCard)

        root.addView(UiKit.spacer(context))

        val modeCard = UiKit.card(context)
        modeCard.addView(
            UiKit.cardTitle(
                context,
                "COMPANION MODE"
            )
        )

        val quietBtn = Button(context)
        quietBtn.text = "Quiet"

        val supportiveBtn = Button(context)
        supportiveBtn.text = "Supportive"

        val explorationBtn = Button(context)
        explorationBtn.text = "Exploration"

        val exploreBtn = Button(context)
        exploreBtn.text = "Explore"

        modeCard.addView(quietBtn)
        modeCard.addView(supportiveBtn)
        modeCard.addView(explorationBtn)
        modeCard.addView(exploreBtn)

        root.addView(modeCard)

        root.addView(UiKit.spacer(context))

        val curveCard = UiKit.card(context)
        curveCard.addView(
            UiKit.cardTitle(
                context,
                "LIVE CURVE"
            )
        )

        val curvePanel = RoastCurvePanel(context)
        curvePanel.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            560
        )
        curveCard.addView(curvePanel)
        root.addView(curveCard)

        root.addView(UiKit.spacer(context))

        val overviewCard = UiKit.card(context)
        overviewCard.addView(
            UiKit.cardTitle(
                context,
                "ROAST OVERVIEW"
            )
        )
        val overviewBody = UiKit.bodyText(context, "")
        overviewCard.addView(overviewBody)
        root.addView(overviewCard)

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

        val sessionCard = UiKit.card(context)
        sessionCard.addView(
            UiKit.cardTitle(
                context,
                "SESSION"
            )
        )
        val sessionBody = UiKit.bodyText(context, "")
        sessionCard.addView(sessionBody)
        root.addView(sessionCard)

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

        val workflowCard = UiKit.card(context)
        workflowCard.addView(
            UiKit.cardTitle(
                context,
                "WORKFLOW"
            )
        )
        val workflowBody = UiKit.bodyText(context, "")
        workflowCard.addView(workflowBody)
        root.addView(workflowCard)

        fun render(
            userRequested: Boolean = false,
            explorationRequested: Boolean = false
        ) {
            val profile = MachineProfiles.HB_M2SE
            val session = RoastSessionEngine.currentState()

            val machineState = MachineStateEngine.buildState(
                powerW = 1200,
                airflowPa = 20,
                drumRpm = 55,
                beanTemp = session.lastBeanTemp,
                ror = session.lastRor,
                elapsedSec = session.lastElapsedSec,
                environmentTemp = 25.0,
                environmentHumidity = 40.0
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

            curvePanel.update()

            overviewBody.text = buildString {
                append("Status\n")
                append(session.status)
                append("\n\n")
                append("Phase\n")
                append(RoastSessionEngine.phaseLabel(session.phase))
                append("\n\n")
                append("Bean Temp\n")
                append(String.format("%.1f ℃", session.lastBeanTemp))
                append("\n\n")
                append("RoR\n")
                append(String.format("%.1f ℃/min", session.lastRor))
                append("\n\n")
                append("Elapsed\n")
                append(session.lastElapsedSec)
                append(" s")
            }

            machineBody.text = buildString {
                append("Profile\n")
                append(profile.name)
                append("\n\n")
                append("Bridge Running\n")
                append(if (MachineBridge.isRunning()) "Yes" else "No")
                append("\n\n")
                append("Power\n")
                append("1200 W")
                append("\n\n")
                append("Airflow\n")
                append("20 Pa")
                append("\n\n")
                append("Drum\n")
                append("55 rpm")
            }

            sessionBody.text = buildString {
                append("First Crack Likely\n")
                append(if (session.firstCrackLikely) "Yes" else "No")
                append("\n\n")
                append("Drop Suggested\n")
                append(if (session.dropSuggested) "Yes" else "No")
                append("\n\n")
                append("Mode\n")
                append(companionState.mode)
                append("\n\n")
                append("Presence\n")
                append(decision.presence)
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

            workflowBody.text = buildString {
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
            MachineBridge.stop()
            RoastSessionEngine.reset()
            MachineBridge.start()

            lastActionLabel = "Start Roast pressed. MachineBridge started."
            render(userRequested = true)
        }

        stopRoastBtn.setOnClickListener {
            MachineBridge.stop()
            lastActionLabel = "Stop Roast pressed. MachineBridge stopped."
            render(userRequested = true)
        }

        askCompanionBtn.setOnClickListener {
            lastActionLabel = "Ask Companion pressed."
            render(userRequested = true)
        }

        exploreBtn.setOnClickListener {
            lastActionLabel = "Explore pressed."
            render(explorationRequested = true)
        }

        render()
        startAutoRefresh { render() }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun startAutoRefresh(
        block: () -> Unit
    ) {
        stopAutoRefresh()

        autoRefreshRunnable = object : Runnable {
            override fun run() {
                block()
                uiHandler.postDelayed(this, 1000L)
            }
        }

        uiHandler.postDelayed(autoRefreshRunnable!!, 1000L)
    }

    private fun stopAutoRefresh() {
        autoRefreshRunnable?.let {
            uiHandler.removeCallbacks(it)
        }
        autoRefreshRunnable = null
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
