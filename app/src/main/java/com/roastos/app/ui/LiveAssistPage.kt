package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.LiveAssistEngine

object LiveAssistPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "LIVE ASSIST"
        title.textSize = 22f

        // Turning Assist
        val turningTitle = TextView(context)
        turningTitle.text = "Turning Assist"

        val predTurningInput = EditText(context)
        predTurningInput.hint = "Pred Turning sec"
        predTurningInput.inputType = InputType.TYPE_CLASS_NUMBER
        predTurningInput.setText("80")

        val actualTurningInput = EditText(context)
        actualTurningInput.hint = "Actual Turning sec"
        actualTurningInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualTurningInput.setText("88")

        val turningBtn = Button(context)
        turningBtn.text = "Assist to Yellow"

        val turningResult = TextView(context)

        // Yellow Assist
        val yellowTitle = TextView(context)
        yellowTitle.text = "Yellow Assist"

        val predYellowInput = EditText(context)
        predYellowInput.hint = "Pred Yellow sec"
        predYellowInput.inputType = InputType.TYPE_CLASS_NUMBER
        predYellowInput.setText("250")

        val actualYellowInput = EditText(context)
        actualYellowInput.hint = "Actual Yellow sec"
        actualYellowInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualYellowInput.setText("265")

        val yellowRorInput = EditText(context)
        yellowRorInput.hint = "Current ROR"
        yellowRorInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        yellowRorInput.setText("13.5")

        val yellowBtn = Button(context)
        yellowBtn.text = "Assist to FC"

        val yellowResult = TextView(context)

        // FC Assist
        val fcTitle = TextView(context)
        fcTitle.text = "First Crack Assist"

        val predFcInput = EditText(context)
        predFcInput.hint = "Pred FC sec"
        predFcInput.inputType = InputType.TYPE_CLASS_NUMBER
        predFcInput.setText("510")

        val actualFcInput = EditText(context)
        actualFcInput.hint = "Actual FC sec"
        actualFcInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualFcInput.setText("520")

        val fcRorInput = EditText(context)
        fcRorInput.hint = "Pre-FC ROR"
        fcRorInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        fcRorInput.setText("9.5")

        val fcBtn = Button(context)
        fcBtn.text = "Assist Development"

        val fcResult = TextView(context)

        root.addView(title)

        root.addView(turningTitle)
        root.addView(predTurningInput)
        root.addView(actualTurningInput)
        root.addView(turningBtn)
        root.addView(turningResult)

        root.addView(yellowTitle)
        root.addView(predYellowInput)
        root.addView(actualYellowInput)
        root.addView(yellowRorInput)
        root.addView(yellowBtn)
        root.addView(yellowResult)

        root.addView(fcTitle)
        root.addView(predFcInput)
        root.addView(actualFcInput)
        root.addView(fcRorInput)
        root.addView(fcBtn)
        root.addView(fcResult)

        container.addView(root)

        turningBtn.setOnClickListener {
            val predTurning = predTurningInput.text.toString().toIntOrNull() ?: 80
            val actualTurning = actualTurningInput.text.toString().toIntOrNull() ?: 80

            val advice = LiveAssistEngine.turningAssist(
                predTurning = predTurning,
                actualTurning = actualTurning
            )

            turningResult.text = """
${advice.deviation}

Heat
${advice.heat}

Air
${advice.airflow}

Note
${advice.note}
            """.trimIndent()
        }

        yellowBtn.setOnClickListener {
            val predYellow = predYellowInput.text.toString().toIntOrNull() ?: 250
            val actualYellow = actualYellowInput.text.toString().toIntOrNull() ?: 250
            val ror = yellowRorInput.text.toString().toDoubleOrNull() ?: 13.0

            val advice = LiveAssistEngine.yellowAssist(
                predYellow = predYellow,
                actualYellow = actualYellow,
                ror = ror
            )

            yellowResult.text = """
${advice.deviation}

Heat
${advice.heat}

Air
${advice.airflow}

Note
${advice.note}
            """.trimIndent()
        }

        fcBtn.setOnClickListener {
            val predFc = predFcInput.text.toString().toIntOrNull() ?: 510
            val actualFc = actualFcInput.text.toString().toIntOrNull() ?: 510
            val ror = fcRorInput.text.toString().toDoubleOrNull() ?: 9.0

            val advice = LiveAssistEngine.fcAssist(
                predFc = predFc,
                actualFc = actualFc,
                ror = ror
            )

            fcResult.text = """
${advice.deviation}

Heat
${advice.heat}

Air
${advice.airflow}

Note
${advice.note}
            """.trimIndent()
        }
    }
}
