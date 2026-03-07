package com.roastos.app.ui

import android.app.Activity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.AppState
import com.roastos.app.CurveEngine
import com.roastos.app.PhaseEngine
import com.roastos.app.RoastEngine

object DashboardPage {

    fun show(activity: Activity, container: LinearLayout) {
        container.removeAllViews()

        val root = LinearLayout(activity)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(activity)
        title.text = "DASHBOARD"
        title.textSize = 22f

        val machineCard = TextView(activity)
        machineCard.text = """
Machine
HB M2SE
Batch 200g
Charge Base 204℃
Max Power 1450W
        """.trimIndent()

        val plannerCard = TextView(activity)
        val batchCard = TextView(activity)
        val liveCard = TextView(activity)
        val curveCard = TextView(activity)
        val correctionCard = TextView(activity)
        val nextStepCard = TextView(activity)

        val resetBatchBtn = Button(activity)
        resetBatchBtn.text = "Reset Current Batch"

        val resetAllBtn = Button(activity)
        resetAllBtn.text = "Reset Planner + Batch"

        val planner = AppState.lastPlannerResult
        val plannerInput = AppState.lastPlannerInput

        if (planner == null || plannerInput == null) {

            plannerCard.text = """
Planner Status
Not Ready

Action
Go to Roast → Planner
Generate roast baseline
            """.trimIndent()

            batchCard.text = """
Batch Overview
No active batch
            """.trimIndent()

            liveCard.text = """
Live Status
Turning -
Yellow -
FC -
Drop -
Pre-FC ROR -
            """.trimIndent()

            curveCard.text = """
Curve Prediction
No prediction yet

Action
Run Planner first
            """.trimIndent()

            correctionCard.text = """
Correction Status
Not ready
            """.trimIndent()

            nextStepCard.text = """
Next Step
Open Roast → Planner
Generate first roast card
            """.trimIndent()

        } else {

            val predTurning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
            val predYellow = planner.h2Sec.toInt()
            val predFc = planner.fcPredSec.toInt()
            val predDrop = planner.dropSec.toInt()

            val liveTurning = AppState.liveActualTurningSec
            val liveYellow = AppState.liveActualYellowSec
            val liveFc = AppState.liveActualFcSec
            val liveDrop = AppState.liveActualDropSec
            val liveRor = AppState.liveActualPreFcRor

            plannerCard.text = """
Planner Status
Ready

Roast Card
Charge ${planner.chargeBT}℃
Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
Drop ${RoastEngine.toMMSS(predDrop.toDouble())}
            """.trimIndent()

            val phase = PhaseEngine.detect(
                predTurning = predTurning,
                predYellow = predYellow,
                predFc = predFc,
                predDrop = predDrop,
                actualTurning = liveTurning,
                actualYellow = liveYellow,
                actualFc = liveFc,
                actualDrop = liveDrop,
                actualPreFcRor = liveRor
            )

            batchCard.text = """
Batch Overview

Current Phase
${phase.currentPhase}

Next Target
${phase.nextTargetLabel} ${RoastEngine.toMMSS(phase.nextTargetSec.toDouble())}

Risk
${phase.riskHint}
            """.trimIndent()

            liveCard.text = """
Live Status

Turning ${liveTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow ${liveYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC ${liveFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop ${liveDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Pre-FC ROR ${liveRor?.let { "%.1f".format(it) } ?: "-"}
            """.trimIndent()

            val curve = CurveEngine.predict(
                predTurning = predTurning,
                predYellow = predYellow,
                predFc = predFc,
                predDrop = predDrop,
                actualTurning = liveTurning,
                actualYellow = liveYellow,
                actualFc = liveFc,
                currentRor = liveRor
            )

            curveCard.text = """
Curve Prediction

Yellow ${RoastEngine.toMMSS(curve.predictedYellowSec.toDouble())}
FC ${RoastEngine.toMMSS(curve.predictedFcSec.toDouble())}
Drop ${RoastEngine.toMMSS(curve.predictedDropSec.toDouble())}
Dev ${curve.predictedDevSec}s

Confidence
${curve.confidence}

Logic
${curve.summary}
            """.trimIndent()

            val correctionReady =
                liveTurning != null &&
                liveYellow != null &&
                liveFc != null &&
                liveDrop != null &&
                liveRor != null

            correctionCard.text = if (correctionReady) {
                """
Correction Status
Ready

Action
Roast → Correction
Generate Batch 2
                """.trimIndent()
            } else {
                """
Correction Status
Waiting For Data
                """.trimIndent()
            }

            val nextStep = when {
                liveTurning == null -> "Go to Roast → Live and record Turning"
                liveYellow == null -> "Go to Roast → Live and record Yellow"
                liveFc == null -> "Go to Roast → Live and record FC"
                liveDrop == null || liveRor == null ->
                    "Go to Roast → Live and finish Development / Drop"
                else -> "Go to Roast → Correction"
            }

            nextStepCard.text = """
Next Step
$nextStep
            """.trimIndent()
        }

        resetBatchBtn.setOnClickListener {
            AppState.resetBatch()
            show(activity, container)
        }

        resetAllBtn.setOnClickListener {
            AppState.resetAll()
            show(activity, container)
        }

        root.addView(title)
        root.addView(machineCard)
        root.addView(plannerCard)
        root.addView(batchCard)
        root.addView(liveCard)
        root.addView(curveCard)
        root.addView(correctionCard)
        root.addView(nextStepCard)
        root.addView(resetBatchBtn)
        root.addView(resetAllBtn)

        container.addView(root)
    }
}
