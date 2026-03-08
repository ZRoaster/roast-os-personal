package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.AppState
import com.roastos.app.BatchSessionEngine
import com.roastos.app.DecisionEngine
import com.roastos.app.RoastCurveEngine
import com.roastos.app.RoastStateModel
import com.roastos.app.RoastTimelineStore

object RoastPage {

    private var autoRefreshEnabled = false
    private var autoRefreshRunnable: Runnable? = null

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST CENTER"))
        root.addView(UiKit.pageSubtitle(context, "Live assist, workflow, timeline tracking, roast curve, and actual input"))
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        statusCard.addView(UiKit.cardTitle(context, "STATUS BAR"))
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(statusBody)
        root.addView(statusCard)
        root.addView(UiKit.spacer(context))

        val workflowCard = UiKit.card(context)
        workflowCard.addView(UiKit.cardTitle(context, "WORKFLOW GUIDE"))
        val workflowBody = UiKit.bodyText(context, "")
        workflowCard.addView(workflowBody)
        root.addView(workflowCard)
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        actionCard.addView(UiKit.cardTitle(context, "ACTIONS"))

        val startBtn = Button(context)
        startBtn.text = "Start Batch"

        val finishBtn = Button(context)
        finishBtn.text = "Finish Batch"

        val resetBtn = Button(context)
        resetBtn.text = "Reset Batch"

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Curve"

        val autoRefreshBtn = Button(context)
        autoRefreshBtn.text = "Auto Refresh OFF"

        actionCard.addView(startBtn)
        actionCard.addView(finishBtn)
        actionCard.addView(resetBtn)
        actionCard.addView(refreshBtn)
        actionCard.addView(autoRefreshBtn)

        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val sessionCard = UiKit.card(context)
        sessionCard.addView(UiKit.cardTitle(context, "BATCH SESSION"))
        val sessionBody = UiKit.bodyText(context, "")
        sessionCard.addView(sessionBody)
        root.addView(sessionCard)
        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "CURVE ENGINE SUMMARY"))
        val summaryBody = UiKit.bodyText(context, "")
        summaryCard.addView(summaryBody)

        val curveCard = UiKit.card(context)
        curveCard.addView(UiKit.cardTitle(context, "ROAST CURVE"))
        val curveView = RoastCurveView(context)
        curveCard.addView(curveView)

        val liveAssistCard = UiKit.card(context)
        liveAssistCard.addView(UiKit.cardTitle(context, "LIVE ASSIST"))
        val liveAssistBody = UiKit.bodyText(context, "")
        liveAssistCard.addView(liveAssistBody)

        fun refreshAll() {
            val curve = RoastCurveEngine.buildFromCurrentState()
            summaryBody.text = curve.summary
            sessionBody.text = BatchSessionEngine.summary()
            statusBody.text = buildTopStatus()
            workflowBody.text = RoastWorkflowGuide.buildText()
            curveView.setCurve(curve)
            liveAssistBody.text = LiveAssistPage.buildLiveAssist()
        }

        LiveAssistPage.attachLiveInputPanel(
            context = context,
            parent = root,
            onDataChanged = {
                refreshAll()
            }
        )
        root.addView(UiKit.spacer(context))

        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))
        root.addView(curveCard)
        root.addView(UiKit.spacer(context))
        root.addView(liveAssistCard)

        fun stopAutoRefresh() {
            autoRefreshRunnable?.let { root.removeCallbacks(it) }
            autoRefreshRunnable = null
            autoRefreshEnabled = false
            autoRefreshBtn.text = "Auto Refresh OFF"
        }

        fun startAutoRefresh() {
            stopAutoRefresh()
            autoRefreshEnabled = true
            autoRefreshBtn.text = "Auto Refresh ON"

            val runnable = object : Runnable {
                override fun run() {
                    if (!autoRefreshEnabled) return
                    refreshAll()
                    root.postDelayed(this, 2000)
                }
            }

            autoRefreshRunnable = runnable
            root.post(runnable)
        }

        startBtn.setOnClickListener {
            BatchSessionEngine.resetCurrentSession()
            BatchSessionEngine.startFromPlanner()
            refreshAll()
        }

        finishBtn.setOnClickListener {
            BatchSessionEngine.finish("Finished from RoastPage")
            refreshAll()
        }

        resetBtn.setOnClickListener {
            stopAutoRefresh()

            AppState.liveActualTurningSec = null
            AppState.liveActualYellowSec = null
            AppState.liveActualFcSec = null
            AppState.liveActualDropSec = null
            AppState.liveActualPreFcRor = null

            RoastTimelineStore.syncActual(
                turningSec = null,
                yellowSec = null,
                fcSec = null,
                dropSec = null,
                ror = null
            )

            BatchSessionEngine.resetCurrentSession()

            RoastStateModel.syncLiveState(
                phase = "Idle",
                ror = 12.0,
                turningSec = null,
                yellowSec = null,
                fcSec = null,
                dropSec = null,
                powerW = 540,
                airflowPa = 10,
                drumRpm = 60
            )

            refreshAll()
        }

        refreshBtn.setOnClickListener {
            refreshAll()
        }

        autoRefreshBtn.setOnClickListener {
            if (autoRefreshEnabled) {
                stopAutoRefresh()
            } else {
                startAutoRefresh()
            }
        }

        refreshAll()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildTopStatus(): String {
        val session = BatchSessionEngine.current()
        val batchStatus = session?.status ?: "Idle"

        val elapsedSec = BatchSessionEngine.currentElapsedSec()
        val elapsedText = if (elapsedSec != null) {
            "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}"
        } else {
            "-"
        }

        val planner = AppState.lastPlannerResult
        val plannerInput = AppState.lastPlannerInput

        val currentPhase = when {
            AppState.liveActualDropSec != null -> "Finished"
            AppState.liveActualFcSec != null -> "Development"
            AppState.liveActualYellowSec != null -> "Maillard / Pre-FC"
            AppState.liveActualTurningSec != null -> "Drying"
            else -> "Idle"
        }

        if (planner == null || plannerInput == null) {
            return """
Batch Status   $batchStatus
Current Phase  $currentPhase
Elapsed        $elapsedText
Risk           -
Action         Run Planner first
            """.trimIndent()
        }

        val predTurning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val predYellow = planner.h2Sec.toInt()
        val predFc = planner.fcPredSec.toInt()
        val predDrop = planner.dropSec.toInt()

        val decision = DecisionEngine.decide(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = AppState.liveActualTurningSec,
            actualYellow = AppState.liveActualYellowSec,
            actualFc = AppState.liveActualFcSec,
            actualDrop = AppState.liveActualDropSec,
            currentRor = AppState.liveActualPreFcRor,
            envTemp = plannerInput.envTemp,
            humidity = plannerInput.envRH,
            pressureKpa = 1013.0,
            density = plannerInput.density,
            moisture = plannerInput.moisture,
            aw = plannerInput.aw,
            heatLevelW = if (RoastStateModel.control.powerW > 0) RoastStateModel.control.powerW else 540,
            airflowPa = if (RoastStateModel.control.airflowPa > 0) RoastStateModel.control.airflowPa else 10,
            drumRpm = if (RoastStateModel.control.drumRpm > 0) RoastStateModel.control.drumRpm else 60
        )

        return """
Batch Status   $batchStatus
Current Phase  ${decision.currentPhase}
Elapsed        $elapsedText
Risk           ${decision.riskLevel}
Action         ${decision.actionNow}
        """.trimIndent()
    }
}
