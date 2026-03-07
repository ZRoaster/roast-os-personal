package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.*
import com.roastos.app.*

object LiveAssistPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val predicted = AppState.lastPlannerResult

        if (predicted == null) {
            val t = TextView(context)
            t.text = "Run Planner first."
            container.addView(t)
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

        val summary = TextView(context)
        summary.text = """
Predicted

Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
""".trimIndent()

        root.addView(title)
        root.addView(summary)

        // Turning
        val turningInput = EditText(context)
        turningInput.hint = "Actual Turning sec"
        turningInput.inputType = InputType.TYPE_CLASS_NUMBER
        turningInput.setText(predTurning.toString())

        val turningBtn = Button(context)
        turningBtn.text = "Turning Assist"

        val turningResult = TextView(context)

        root.addView(turningInput)
        root.addView(turningBtn)
        root.addView(turningResult)

        // Yellow
        val yellowInput = EditText(context)
        yellowInput.hint = "Actual Yellow sec"
        yellowInput.inputType = InputType.TYPE_CLASS_NUMBER
        yellowInput.setText(predYellow.toString())

        val yellowRor = EditText(context)
        yellowRor.hint = "Current ROR"
        yellowRor.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        yellowRor.setText("13")

        val yellowBtn = Button(context)
        yellowBtn.text = "Yellow Assist"

        val yellowResult = TextView(context)

        root.addView(yellowInput)
        root.addView(yellowRor)
        root.addView(yellowBtn)
        root.addView(yellowResult)

        // FC
        val fcInput = EditText(context)
        fcInput.hint = "Actual FC sec"
        fcInput.inputType = InputType.TYPE_CLASS_NUMBER
        fcInput.setText(predFc.toString())

        val fcRor = EditText(context)
        fcRor.hint = "Pre-FC ROR"
        fcRor.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        fcRor.setText("9")

        val fcBtn = Button(context)
        fcBtn.text = "FC Assist"

        val fcResult = TextView(context)

        root.addView(fcInput)
        root.addView(fcRor)
        root.addView(fcBtn)
        root.addView(fcResult)

        container.addView(root)

        turningBtn.setOnClickListener {

            val actualTurning =
                turningInput.text.toString().toIntOrNull() ?: predTurning

            AppState.liveActualTurningSec = actualTurning

            val advice = LiveAssistEngine.turningAssist(
                predTurning,
                actualTurning
            )

            turningResult.text =
                "${advice.deviation}\n${advice.heat}\n${advice.airflow}"
        }

        yellowBtn.setOnClickListener {

            val actualYellow =
                yellowInput.text.toString().toIntOrNull() ?: predYellow

            val ror =
                yellowRor.text.toString().toDoubleOrNull() ?: 13.0

            AppState.liveActualYellowSec = actualYellow

            val advice = LiveAssistEngine.yellowAssist(
                predYellow,
                actualYellow,
                ror
            )

            yellowResult.text =
                "${advice.deviation}\n${advice.heat}\n${advice.airflow}"
        }

        fcBtn.setOnClickListener {

            val actualFc =
                fcInput.text.toString().toIntOrNull() ?: predFc

            val ror =
                fcRor.text.toString().toDoubleOrNull() ?: 9.0

            AppState.liveActualFcSec = actualFc
            AppState.liveActualPreFcRor = ror

            val advice = LiveAssistEngine.fcAssist(
                predFc,
                actualFc,
                ror
            )

            fcResult.text =
                "${advice.deviation}\n${advice.heat}\n${advice.airflow}"
        }
    }
}
