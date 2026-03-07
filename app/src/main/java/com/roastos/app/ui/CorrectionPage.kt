package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.BatchActualInput
import com.roastos.app.CorrectionEngine
import com.roastos.app.PlannerInput
import com.roastos.app.RoastEngine

object CorrectionPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "BATCH CORRECTION"
        title.textSize = 22f

        val processInput = EditText(context)
        processInput.hint = "Process"
        processInput.setText("washed")

        val densityInput = EditText(context)
        densityInput.hint = "Density"
        densityInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        densityInput.setText("840")

        val moistureInput = EditText(context)
        moistureInput.hint = "Moisture"
        moistureInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        moistureInput.setText("10.5")

        val awInput = EditText(context)
        awInput.hint = "aw"
        awInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        awInput.setText("0.55")

        val envTempInput = EditText(context)
        envTempInput.hint = "Environment Temp"
        envTempInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        envTempInput.setText("22")

        val humidityInput = EditText(context)
        humidityInput.hint = "Humidity"
        humidityInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        humidityInput.setText("40")

        val roastLevelInput = EditText(context)
        roastLevelInput.hint = "Roast Level"
        roastLevelInput.setText("light_medium")

        val orientationInput = EditText(context)
        orientationInput.hint = "Orientation"
        orientationInput.setText("clean")

        val batchInput = EditText(context)
        batchInput.hint = "Batch Number"
        batchInput.inputType = InputType.TYPE_CLASS_NUMBER
        batchInput.setText("1")

        val ttInput = EditText(context)
        ttInput.hint = "Pred Turning sec"
        ttInput.inputType = InputType.TYPE_CLASS_NUMBER
        ttInput.setText("80")

        val tyInput = EditText(context)
        tyInput.hint = "Pred Yellow sec"
        tyInput.inputType = InputType.TYPE_CLASS_NUMBER
        tyInput.setText("250")

        val actualTurningInput = EditText(context)
        actualTurningInput.hint = "Actual Turning sec"
        actualTurningInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualTurningInput.setText("88")

        val actualYellowInput = EditText(context)
        actualYellowInput.hint = "Actual Yellow sec"
        actualYellowInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualYellowInput.setText("265")

        val actualFcInput = EditText(context)
        actualFcInput.hint = "Actual FC sec"
        actualFcInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualFcInput.setText("515")

        val actualDropInput = EditText(context)
        actualDropInput.hint = "Actual Drop sec"
        actualDropInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualDropInput.setText("600")

        val actualPreFcRorInput = EditText(context)
        actualPreFcRorInput.hint = "Actual Pre-FC ROR"
        actualPreFcRorInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        actualPreFcRorInput.setText("9.5")

        val calculateBtn = Button(context)
        calculateBtn.text = "Generate Batch 2 Strategy"

        val resultView = TextView(context)

        root.addView(title)

        root.addView(processInput)
        root.addView(densityInput)
        root.addView(moistureInput)
        root.addView(awInput)
        root.addView(envTempInput)
        root.addView(humidityInput)
        root.addView(roastLevelInput)
        root.addView(orientationInput)
        root.addView(batchInput)
        root.addView(ttInput)
        root.addView(tyInput)

        root.addView(actualTurningInput)
        root.addView(actualYellowInput)
        root.addView(actualFcInput)
        root.addView(actualDropInput)
        root.addView(actualPreFcRorInput)

        root.addView(calculateBtn)
        root.addView(resultView)

        container.addView(root)

        calculateBtn.setOnClickListener {

            val plannerInput = PlannerInput(
                process = processInput.text.toString().ifBlank { "washed" },
                density = densityInput.text.toString().toDoubleOrNull() ?: 840.0,
                moisture = moistureInput.text.toString().toDoubleOrNull() ?: 10.5,
                aw = awInput.text.toString().toDoubleOrNull() ?: 0.55,
                envTemp = envTempInput.text.toString().toDoubleOrNull() ?: 22.0,
                envRH = humidityInput.text.toString().toDoubleOrNull() ?: 40.0,
                roastLevel = roastLevelInput.text.toString().ifBlank { "light_medium" },
                orientation = orientationInput.text.toString().ifBlank { "clean" },
                purpose = "pourover",
                batchNum = batchInput.text.toString().toIntOrNull() ?: 1,
                beanSize = "normal",
                freshness = "fresh",
                mode = "M2",
                learnM = 5.5,
                learnK = 26.0,
                learnW = 0.65,
                ttSec = ttInput.text.toString().toIntOrNull() ?: 80,
                tySec = tyInput.text.toString().toIntOrNull() ?: 250
            )

            val predicted = RoastEngine.calcCard(plannerInput)

            val actual = BatchActualInput(
                turningSec = actualTurningInput.text.toString().toIntOrNull() ?: 88,
                yellowSec = actualYellowInput.text.toString().toIntOrNull() ?: 265,
                firstCrackSec = actualFcInput.text.toString().toIntOrNull() ?: 515,
                dropSec = actualDropInput.text.toString().toIntOrNull() ?: 600,
                preFcRor = actualPreFcRorInput.text.toString().toDoubleOrNull() ?: 9.5
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
