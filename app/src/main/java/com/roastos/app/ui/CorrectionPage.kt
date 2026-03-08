package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.AdaptiveCalibrationEngine
import com.roastos.app.AppState
import com.roastos.app.RoastEngine

object CorrectionPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val planner = AppState.lastPlannerResult
        val plannerInput = AppState.lastPlannerInput

        if (planner == null || plannerInput == null) {
            val t = TextView(context)
            t.text = "Run Planner first."
            container.addView(t)
            return
        }

        val actualTurning = AppState.liveActualTurningSec
        val actualYellow = AppState.liveActualYellowSec
        val actualFc = AppState.liveActualFcSec
        val actualDrop = AppState.liveActualDropSec
        val actualRor = AppState.liveActualPreFcRor

        val predTurning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val predYellow = planner.h2Sec.toInt()
        val predFc = planner.fcPredSec.toInt()
        val predDrop = planner.dropSec.toInt()

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "BATCH CORRECTION"
        title.textSize = 22f

        val baselineCard = TextView(context)
        baselineCard.text = """
Planner Baseline

Charge ${planner.chargeBT}℃
Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
Drop ${RoastEngine.toMMSS(predDrop.toDouble())}
        """.trimIndent()

        val liveCard = TextView(context)
        liveCard.text = """
Actual Batch Data

Turning ${actualTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow ${actualYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC ${actualFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop ${actualDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Pre-FC ROR ${actualRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()

        val resultCard = TextView(context)
        val learningCard = TextView(context)

        val runBtn = Button(context)
        runBtn.text = "Generate Batch 2 Correction"

        root.addView(title)
        root.addView(baselineCard)
        root.addView(liveCard)
        root.addView(runBtn)
        root.addView(resultCard)
        root.addView(learningCard)

        container.addView(root)

        runBtn.setOnClickListener {

            if (actualTurning == null ||
                actualYellow == null ||
                actualFc == null ||
                actualDrop == null ||
                actualRor == null
            ) {
                resultCard.text = """
Correction Status
Not ready

Needed
${if (actualTurning == null) "• Turning actual\n" else ""}${if (actualYellow == null) "• Yellow actual\n" else ""}${if (actualFc == null) "• FC actual\n" else ""}${if (actualDrop == null) "• Drop actual\n" else ""}${if (actualRor == null) "• Pre-FC ROR" else ""}
                """.trimIndent()

                learningCard.text = ""
                return@setOnClickListener
            }

            val turningDelta = actualTurning - predTurning
            val yellowDelta = actualYellow - predYellow
            val fcDelta = actualFc - predFc
            val dropDelta = actualDrop - predDrop

            val chargeCorrection = chargeCorrection(turningDelta)
            val heatCorrection = heatCorrection(yellowDelta, actualRor)
            val airCorrection = airCorrection(yellowDelta, actualRor)
            val devCorrection = devCorrection(fcDelta, actualRor)
            val diagnosis = diagnosis(turningDelta, yellowDelta, fcDelta, actualRor)

            val batch2Charge = planner.chargeBT + chargeCorrection
            val predictedBatch2Fc = predFc - (fcDelta * 0.45).toInt()
            val predictedBatch2Drop = predDrop - (dropDelta * 0.45).toInt()

            resultCard.text = """
Correction Diagnosis

Turning Δ ${formatSigned(turningDelta)}s
Yellow Δ ${formatSigned(yellowDelta)}s
FC Δ ${formatSigned(fcDelta)}s
Drop Δ ${formatSigned(dropDelta)}s
Pre-FC ROR ${"%.1f".format(actualRor)}

Diagnosis
$diagnosis

Batch 2 Action Card

Charge
${planner.chargeBT}℃ → ${batch2Charge}℃

Front-End
${chargeCorrectionHint(chargeCorrection)}

Middle Phase
$heatCorrection
$airCorrection

Development
$devCorrection

Batch 2 Predicted Targets
FC ${RoastEngine.toMMSS(predictedBatch2Fc.toDouble())}
Drop ${RoastEngine.toMMSS(predictedBatch2Drop.toDouble())}
            """.trimIndent()

            val update = AdaptiveCalibrationEngine.update(
                current = AppState.calibrationState,
                predictedFcSec = predFc,
                predictedDropSec = predDrop,
                actualFcSec = actualFc,
                actualDropSec = actualDrop,
                actualPreFcRor = actualRor
            )

            AppState.calibrationState = update.newState

            learningCard.text = """
Adaptive Learning

${update.summary}
            """.trimIndent()
        }
    }

    private fun chargeCorrection(turningDelta: Int): Int {
        return when {
            turningDelta >= 12 -> 2
            turningDelta >= 6 -> 1
            turningDelta <= -12 -> -2
            turningDelta <= -6 -> -1
            else -> 0
        }
    }

    private fun chargeCorrectionHint(correction: Int): String {
        return when {
            correction > 0 -> "Charge +${correction}℃ to recover front-end energy"
            correction < 0 -> "Charge ${correction}℃ to soften front-end push"
            else -> "Hold charge temperature"
        }
    }

    private fun heatCorrection(yellowDelta: Int, actualRor: Double): String {
        return when {
            yellowDelta >= 15 -> "Heat +40W to +60W through Maillard"
            yellowDelta <= -15 -> "Heat -40W to -60W before FC"
            actualRor > 10.5 -> "Heat -40W around pre-FC"
            actualRor < 7.5 -> "Heat +40W to protect development"
            else -> "Heat Hold"
        }
    }

    private fun airCorrection(yellowDelta: Int, actualRor: Double): String {
        return when {
            yellowDelta <= -15 || actualRor > 10.5 -> "Air +1Pa to +2Pa"
            yellowDelta >= 15 || actualRor < 7.5 -> "Air Hold / slight delay"
            else -> "Air Hold"
        }
    }

    private fun devCorrection(fcDelta: Int, actualRor: Double): String {
        return when {
            actualRor > 10.5 -> "Reduce development energy and protect finish"
            actualRor < 7.5 -> "Preserve development energy and avoid crash"
            fcDelta >= 15 -> "Do not drag post-crack too long"
            fcDelta <= -15 -> "Protect sweetness and avoid harsh finish"
            else -> "Hold development structure"
        }
    }

    private fun diagnosis(
        turningDelta: Int,
        yellowDelta: Int,
        fcDelta: Int,
        actualRor: Double
    ): String {
        return when {
            turningDelta > 8 && yellowDelta > 12 ->
                "Front-end energy weak, drying and middle phase both late"
            turningDelta < -8 && yellowDelta < -12 ->
                "Front-end push too strong, early acceleration compressed structure"
            yellowDelta > 15 && actualRor < 9.0 ->
                "Middle phase weak, FC likely delayed and cup may flatten"
            yellowDelta < -15 && actualRor > 10.5 ->
                "Middle phase too aggressive, pre-FC spike risk high"
            fcDelta > 15 && actualRor < 8.0 ->
                "Crack arrived late with weak energy, development support insufficient"
            fcDelta < -15 && actualRor > 10.5 ->
                "Crack arrived early with strong energy, overshoot risk elevated"
            else ->
                "Batch was near target, only moderate correction needed"
        }
    }

    private fun formatSigned(value: Int): String {
        return if (value > 0) "+$value" else value.toString()
    }
}
