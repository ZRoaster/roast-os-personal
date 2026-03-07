package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.*
import com.roastos.app.*

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

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "BATCH CORRECTION"
        title.textSize = 22f

        root.addView(title)

        val turningInput = EditText(context)
        turningInput.hint = "Turning sec"
        turningInput.inputType = InputType.TYPE_CLASS_NUMBER

        val yellowInput = EditText(context)
        yellowInput.hint = "Yellow sec"
        yellowInput.inputType = InputType.TYPE_CLASS_NUMBER

        val fcInput = EditText(context)
        fcInput.hint = "FC sec"
        fcInput.inputType = InputType.TYPE_CLASS_NUMBER

        val dropInput = EditText(context)
        dropInput.hint = "Drop sec"
        dropInput.inputType = InputType.TYPE_CLASS_NUMBER
        dropInput.setText(predicted.dropSec.toInt().toString())

        val rorInput = EditText(context)
        rorInput.hint = "Pre-FC ROR"
        rorInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        // 自动带入 Live 数据
        AppState.liveActualTurningSec?.let {
            turningInput.setText(it.toString())
        }

        AppState.liveActualYellowSec?.let {
            yellowInput.setText(it.toString())
        }

        AppState.liveActualFcSec?.let {
            fcInput.setText(it.toString())
        }

        AppState.liveActualPreFcRor?.let {
            rorInput.setText(it.toString())
        }

        val runBtn = Button(context)
        runBtn.text = "Generate Batch 2"

        val result = TextView(context)

        root.addView(turningInput)
        root.addView(yellowInput)
        root.addView(fcInput)
        root.addView(dropInput)
        root.addView(rorInput)
        root.addView(runBtn)
        root.addView(result)

        container.addView(root)

        runBtn.setOnClickListener {

            val actual = BatchActualInput(

                turningSec =
                turningInput.text.toString().toIntOrNull() ?: 80,

                yellowSec =
                yellowInput.text.toString().toIntOrNull() ?: 250,

                firstCrackSec =
                fcInput.text.toString().toIntOrNull() ?: 510,

                dropSec =
                dropInput.text.toString().toIntOrNull()
                    ?: predicted.dropSec.toInt(),

                preFcRor =
                rorInput.text.toString().toDoubleOrNull() ?: 9.0
            )

            val correction = CorrectionEngine.correct(
                plannerInput,
                predicted,
                actual,
                plannerInput.batchNum
            )

            result.text = correction.batch2ExecutionCard
        }
    }
}
