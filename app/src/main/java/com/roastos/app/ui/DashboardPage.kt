package com.roastos.app.ui

import android.app.Activity
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.AppState
import com.roastos.app.RoastEngine

object DashboardPage {

    fun show(activity: Activity, container: LinearLayout) {
        container.removeAllViews()

        val title = TextView(activity)
        title.text = "Dashboard"
        title.textSize = 22f

        val machineCard = TextView(activity)
        machineCard.text = """
Machine
HB M2SE
Batch 200g
Charge Base 204℃
Max Power 1450W
        """.trimIndent()

        val planner = AppState.lastPlannerResult
        val plannerCard = TextView(activity)

        if (planner == null) {
            plannerCard.text = """
Planner Status
No planner card yet.

Action
Go to Roast → Planner
Generate first roast card
            """.trimIndent()
        } else {
            val turningSec = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
            val yellowSec = planner.h2Sec.toInt()
            val fcSec = planner.fcPredSec.toInt()
            val dropSec = planner.dropSec.toInt()

            plannerCard.text = """
Planner Status
Ready

Latest Roast Card
Charge ${planner.chargeBT}℃
Turning ${RoastEngine.toMMSS(turningSec.toDouble())}
Yellow ${RoastEngine.toMMSS(yellowSec.toDouble())}
FC ${RoastEngine.toMMSS(fcSec.toDouble())}
Drop ${RoastEngine.toMMSS(dropSec.toDouble())}
            """.trimIndent()
        }

        val liveTurning = AppState.liveActualTurningSec
        val liveYellow = AppState.liveActualYellowSec
        val liveFc = AppState.liveActualFcSec
        val liveDrop = AppState.liveActualDropSec
        val liveRor = AppState.liveActualPreFcRor

        val liveProgress = buildString {
            appendLine("Live Status")

            appendLine(
                "Turning: " + (
                    liveTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"
                )
            )

            appendLine(
                "Yellow: " + (
                    liveYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"
                )
            )

            appendLine(
                "FC: " + (
                    liveFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"
                )
            )

            appendLine(
                "Drop: " + (
                    liveDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"
                )
            )

            append(
                "Pre-FC ROR: " + (
                    liveRor?.let { "%.1f".format(it) } ?: "-"
                )
            )
        }

        val liveCard = TextView(activity)
        liveCard.text = liveProgress

        val correctionReady =
            planner != null &&
            liveTurning != null &&
            liveYellow != null &&
            liveFc != null &&
            liveDrop != null &&
            liveRor != null

        val correctionCard = TextView(activity)
        correctionCard.text = if (correctionReady) {
            """
Correction Status
Ready

Action
Go to Roast → Correction
Generate Batch 2 in one tap
            """.trimIndent()
        } else {
            """
Correction Status
Not ready

Needed
${if (planner == null) "• Planner card" else ""}
${if (liveTurning == null) "• Turning actual" else ""}
${if (liveYellow == null) "• Yellow actual" else ""}
${if (liveFc == null) "• FC actual" else ""}
${if (liveDrop == null) "• Drop actual" else ""}
${if (liveRor == null) "• Pre-FC ROR" else ""}
            """.trimIndent()
        }

        val engineCard = TextView(activity)
        engineCard.text = """
Core Engines
Planner → RoastEngine
Live → LiveAssistEngine
Correction → CorrectionEngine
State → AppState
        """.trimIndent()

        container.addView(title)
        container.addView(machineCard)
        container.addView(plannerCard)
        container.addView(liveCard)
        container.addView(correctionCard)
        container.addView(engineCard)
    }
}
