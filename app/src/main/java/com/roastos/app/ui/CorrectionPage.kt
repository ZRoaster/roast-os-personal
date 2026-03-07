package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.AppState
import com.roastos.app.BatchActualInput
import com.roastos.app.CorrectionEngine

object CorrectionPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val plannerInput = AppState.lastPlannerInput
        val predicted = AppState.lastPlannerResult

        if (plannerInput == null || predicted == null) {
            val text = TextView(context)
            text.text = "No planner state found. Please run Planner first."
            container.addView(text)
            return
        }

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "BATCH CORRECTION"
        title.textSize = 22f

        val stateSummary = TextView(context)
        stateSummary.text = """
Loaded from Planner

Pred FC ${predicted.fcPredSec.toInt()} sec
Pred Drop ${predicted.dropSec.toInt()} sec
Charge ${predicted.chargeBT}℃
        """.trimIndent()

        val actualTurningInput = EditText(context)
        actualTurningInput.hint = "Actual Turning sec"
        actualTurningInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualTurningInput.setText((predicted.h1Sec - 60.0).toInt().toString())

        val actualYellowInput = EditText(context)
        actualYellowInput.hint = "Actual Yellow sec"
        actualYellowInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualYellowInput.setText(predicted.h2Sec.toInt().toString())

        val actualFcInput = EditText(context)
        actualFcInput.hint = "Actual FC sec"
        actualFcInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualFcInput.setText(predicted.fcPredSec.toInt().toString())

        val actualDropInput = EditText(context)
        actualDropInput.hint = "Actual Drop sec"
        actualDropInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualDropInput.setText(predicted.dropSec.toInt().toString())

        val actualPreFcRorInput = EditText(context)
        actualPreFcRorInput.hint = "Actual Pre-FC ROR"
        actualPreFcRorInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        actualPreFcRorInput.setText(predicted.rorFull5[3].toString())

        val calculateBtn = Button(context)
        calculateBtn.text = "Generate Batch 2 Strategy"

        val resultView = TextView(context)

        root.addView(title)
        root.addView(stateSummary)
        root.addView(actualTurningInput)
        root.addView(actualYellowInput)
        root.addView(actualFcInput)
        root.addView(actualDropInput)
        root.addView(actualPreFcRorInput)
        root.addView(calculateBtn)
        root.addView(resultView)

        container.addView(root)

        calculateBtn.setOnClickListener {

            val actual = BatchActualInput(
                turningSec = actualTurningInput.text.toString().toIntOrNull()
                    ?: (predicted.h1Sec - 60.0).toInt(),
                yellowSec = actualYellowInput.text.toString().toIntOrNull()
                    ?: predicted.h2Sec.toInt(),
                firstCrackSec = actualFcInput.text.toString().toIntOrNull()
                    ?: predicted.fcPredSec.toInt(),
                dropSec = actualDropInput.text.toString().toIntOrNull()
                    ?: predicted.dropSec.toInt(),
                preFcRor = actualPreFcRorInput.text.toString().toDoubleOrNull()
                    ?: predicted.rorFull5[3]
            )

            val correction = CorrectionEngine.correct(
                plannerInput = plannerInput,
                predicted = predicted,
                actual = actual,
                batchIndex = plannerInput.batchNum
            )

            resultView.text = """
Stage
${correction.stageLabel}

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
${correction.diagnosis.joinToString("\n")}

Actions
${correction.actions.joinToString("\n")}

Batch 2 Execution Card
${correction.batch2ExecutionCard}
            """.trimIndent()
        }
    }
}
