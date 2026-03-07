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
Loaded from Planner

Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
        """.trimIndent()

        root.addView(title)
        root.addView(summary)

        // Turning
        val turningTitle = TextView(context)
        turningTitle.text = "Turning Assist"

        val turningInput = EditText(context)
        turningInput.hint = "Actual Turning sec"
        turningInput.inputType = InputType.TYPE_CLASS_NUMBER
        turningInput.setText(predTurning.toString())

        val turningBtn = Button(context)
        turningBtn.text = "Turning Assist"

        val turningResult = TextView(context)

        root.addView(turningTitle)
        root.addView(turningInput)
        root.addView(turningBtn)
        root.addView(turningResult)

        // Yellow
        val yellowTitle = TextView(context)
        yellowTitle.text = "Yellow Assist"

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

        root.addView(yellowTitle)
        root.addView(yellowInput)
        root.addView(yellowRor)
        root.addView(yellowBtn)
        root.addView(yellowResult)

        // FC
        val fcTitle = TextView(context)
        fcTitle.text = "First Crack Assist"

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

        root.addView(fcTitle)
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

            val diff = actualTurning - predTurning

            val targetNext = when {
                diff > 8 -> "Pull Yellow back toward ${RoastEngine.toMMSS((predYellow + diff / 2.0).toDouble())}"
                diff < -8 -> "Prevent Yellow from arriving too early"
                else -> "Hold Yellow near original window"
            }

            val risk = when {
                diff > 8 -> "Front-end energy short / Yellow delay risk"
                diff < -8 -> "Mid-phase push too fast risk"
                else -> "No immediate front-end risk"
            }

            turningResult.text = """
Current State
Pred Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Actual Turning ${RoastEngine.toMMSS(actualTurning.toDouble())}

Deviation
${advice.deviation}

Action Now
Heat: ${advice.heat}
Air: ${advice.airflow}

Target Next
$targetNext

Risk
$risk
            """.trimIndent()
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

            val diff = actualYellow - predYellow

            val targetNext = when {
                diff > 15 -> "Pull FC back earlier with stronger middle push"
                diff < -15 -> "Delay FC slightly and protect pre-crack stability"
                ror > 14.0 -> "Reduce mid-phase momentum before FC"
                else -> "Keep FC on original prediction path"
            }

            val risk = when {
                diff > 15 -> "Late FC / baked middle risk"
                diff < -15 -> "Pre-FC overshoot risk"
                ror > 14.0 -> "High ROR spike risk"
                else -> "Middle phase relatively stable"
            }

            yellowResult.text = """
Current State
Pred Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
Actual Yellow ${RoastEngine.toMMSS(actualYellow.toDouble())}
Current ROR ${"%.1f".format(ror)}

Deviation
${advice.deviation}

Action Now
Heat: ${advice.heat}
Air: ${advice.airflow}

Target Next
$targetNext

Risk
$risk
            """.trimIndent()
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

            val diff = actualFc - predFc

            val targetNext = when {
                ror > 10.0 -> "Stabilize development and prevent overshoot"
                ror < 7.0 -> "Support development energy and avoid crash"
                diff > 15 -> "Avoid dragging development too long"
                diff < -15 -> "Protect sweetness and avoid sharp finish"
                else -> "Hold development in controlled window"
            }

            val risk = when {
                ror > 10.0 -> "Development overshoot risk"
                ror < 7.0 -> "Development crash / hollow finish risk"
                diff > 15 -> "Late crack / flat finish risk"
                diff < -15 -> "Fast crack / sharp finish risk"
                else -> "Development risk moderate"
            }

            turningInput.text?.toString()?.toIntOrNull()?.let {
                AppState.liveActualTurningSec = it
            }

            yellowInput.text?.toString()?.toIntOrNull()?.let {
                AppState.liveActualYellowSec = it
            }

            fcResult.text = """
Current State
Pred FC ${RoastEngine.toMMSS(predFc.toDouble())}
Actual FC ${RoastEngine.toMMSS(actualFc.toDouble())}
Pre-FC ROR ${"%.1f".format(ror)}

Deviation
${advice.deviation}

Action Now
Heat: ${advice.heat}
Air: ${advice.airflow}

Target Next
$targetNext

Risk
$risk
            """.trimIndent()
        }
    }
}
