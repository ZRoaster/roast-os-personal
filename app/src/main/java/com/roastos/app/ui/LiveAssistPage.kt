package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.AppState
import com.roastos.app.LiveAssistEngine
import com.roastos.app.RoastEngine

object LiveAssistPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val predicted = AppState.lastPlannerResult
        if (predicted == null) {
            val text = TextView(context)
            text.text = "No planner state found. Please run Planner first."
            container.addView(text)
            return
        }

        val predTurning = (predicted.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val predYellow = predicted.h2Sec.toInt()
        val predFc = predicted.fcPredSec.toInt()

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "LIVE ASSIST"
        title.textSize = 22f

        val predSummary = TextView(context)
        predSummary.text = """
Loaded from Planner

Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
        """.trimIndent()

        // Turning Assist
        val turningTitle = TextView(context)
        turningTitle.text = "Turning Assist"

        val actualTurningInput = EditText(context)
        actualTurningInput.hint = "Actual Turning sec"
        actualTurningInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualTurningInput.setText(predTurning.toString())

        val turningBtn = Button(context)
        turningBtn.text = "Assist to Yellow"

        val turningResult = TextView(context)

        // Yellow Assist
        val yellowTitle = TextView(context)
        yellowTitle.text = "Yellow Assist"

        val actualYellowInput = EditText(context)
        actualYellowInput.hint = "Actual Yellow sec"
        actualYellowInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualYellowInput.setText(predYellow.toString())

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

        val actualFcInput = EditText(context)
        actualFcInput.hint = "Actual FC sec"
        actualFcInput.inputType = InputType.TYPE_CLASS_NUMBER
        actualFcInput.setText(predFc.toString())

        val fcRorInput = EditText(context)
        fcRorInput.hint = "Pre-FC ROR"
        fcRorInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        fcRorInput.setText("9.5")

        val fcBtn = Button(context)
        fcBtn.text = "Assist Development"

        val fcResult = TextView(context)

        root.addView(title)
        root.addView(predSummary)

        root.addView(turningTitle)
        root.addView(actualTurningInput)
        root.addView(turningBtn)
        root.addView(turningResult)

        root.addView(yellowTitle)
        root.addView(actualYellowInput)
        root.addView(yellowRorInput)
        root.addView(yellowBtn)
        root.addView(yellowResult)

        root.addView(fcTitle)
        root.addView(actualFcInput)
        root.addView(fcRorInput)
        root.addView(fcBtn)
        root.addView(fcResult)

        container.addView(root)

        turningBtn.setOnClickListener {
            val actualTurning = actualTurningInput.text.toString().toIntOrNull() ?: predTurning

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
            val actualYellow = actualYellowInput.text.toString().toIntOrNull() ?: predYellow
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
            val actualFc = actualFcInput.text.toString().toIntOrNull() ?: predFc
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
