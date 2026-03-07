package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.AppState
import com.roastos.app.BatchActualInput
import com.roastos.app.CorrectionEngine
import com.roastos.app.RoastEngine

object CorrectionPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val plannerInput = AppState.lastPlannerInput
        val predicted = AppState.lastPlannerResult

        if (plannerInput == null || predicted == null) {
            val t = TextView(context)
            t.text = "Run Planner first."
            container.addView(t)
            return
        }

        val actualTurning = AppState.liveActualTurningSec
        val actualYellow = AppState.liveActualYellowSec
        val actualFc = AppState.liveActualFcSec
        val actualRor = AppState.liveActualPreFcRor
        val actualDrop = AppState.liveActualDropSec

        if (actualTurning == null || actualYellow == null || actualFc == null || actualRor == null || actualDrop == null) {
            val t = TextView(context)
            t.text = "Run Live Assist first and complete Turning / Yellow / FC / Drop."
            container.addView(t)
            return
        }

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "BATCH CORRECTION"
        title.textSize = 22f

        val stateSummary = TextView(context)
        stateSummary.text = """
Loaded State

Process ${predicted.ptLabel}
Charge ${predicted.chargeBT}℃
Pred FC ${RoastEngine.toMMSS(predicted.fcPredSec)}
Pred Drop ${RoastEngine.toMMSS(predicted.dropSec)}

Actual Turning ${RoastEngine.toMMSS(actualTurning.toDouble())}
Actual Yellow ${RoastEngine.toMMSS(actualYellow.toDouble())}
Actual FC ${RoastEngine.toMMSS(actualFc.toDouble())}
Actual Drop ${RoastEngine.toMMSS(actualDrop.toDouble())}
Actual Pre-FC ROR ${"%.1f".format(actualRor)}
        """.trimIndent()

        val runBtn = Button(context)
        runBtn.text = "Generate Batch 2"

        val result = TextView(context)

        root.addView(title)
        root.addView(stateSummary)
        root.addView(runBtn)
        root.addView(result)

        container.addView(root)

        runBtn.setOnClickListener {

            val actual = BatchActualInput(
                turningSec = actualTurning,
                yellowSec = actualYellow,
                firstCrackSec = actualFc,
                dropSec = actualDrop,
                preFcRor = actualRor
            )

            val correction = CorrectionEngine.correct(
                plannerInput = plannerInput,
                predicted = predicted,
                actual = actual,
                batchIndex = plannerInput.batchNum
            )

            val summary = when {
                correction.deltaFcSec > 15 ->
                    "Batch 1 ran slow into FC. Batch 2 should recover middle-to-late momentum."
                correction.deltaFcSec < -15 ->
                    "Batch 1 reached FC too early. Batch 2 should reduce push before crack."
                correction.deltaPreFcRor > 1.0 ->
                    "Batch 1 carried too much energy into crack. Batch 2 should protect development."
                correction.deltaPreFcRor < -1.0 ->
                    "Batch 1 lacked energy before crack. Batch 2 should preserve more momentum."
                else ->
                    "Batch 1 stayed close to target. Batch 2 needs only light correction."
            }

            val diagnosisText = if (correction.diagnosis.isEmpty()) {
                "No major diagnosis."
            } else {
                correction.diagnosis.joinToString("\n") { "• $it" }
            }

            val actionsText = if (correction.actions.isEmpty()) {
                "• Keep core plan and apply only small manual adjustments."
            } else {
                correction.actions.joinToString("\n") { "• $it" }
            }

            val riskFocus = when {
                correction.deltaPreFcRor > 1.0 -> "Pre-FC overshoot risk"
                correction.deltaPreFcRor < -1.0 -> "Energy collapse risk"
                correction.deltaFcSec > 15 -> "Late crack / flat finish risk"
                correction.deltaFcSec < -15 -> "Fast crack / sharp finish risk"
                else -> "Moderate replay risk"
            }

            result.text = """
BATCH 2 CORRECTION CARD

Summary
$summary

Deviation
Turning Δ ${correction.deltaTurningSec}s
Yellow Δ ${correction.deltaYellowSec}s
FC Δ ${correction.deltaFcSec}s
Drop Δ ${correction.deltaDropSec}s
Pre-FC ROR Δ ${"%.1f".format(correction.deltaPreFcRor)}

Bias Scores
Heat ${"%.2f".format(correction.heatBiasScore)}
Airflow ${"%.2f".format(correction.airflowBiasScore)}
Inertia ${"%.2f".format(correction.inertiaBiasScore)}

Diagnosis
$diagnosisText

Batch 2 Actions
$actionsText

Risk Focus
$riskFocus

Execution Card
${correction.batch2ExecutionCard}
            """.trimIndent()
        }
    }
}
