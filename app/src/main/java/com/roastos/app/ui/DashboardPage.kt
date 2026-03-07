package com.roastos.app.ui

import android.app.Activity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.AppState
import com.roastos.app.RoastEngine

object DashboardPage {

    fun show(activity: Activity, container: LinearLayout) {

        container.removeAllViews()

        val root = LinearLayout(activity)
        root.orientation = LinearLayout.VERTICAL

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
No roast card yet

Action
Roast → Planner
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

        val liveCard = TextView(activity)

        liveCard.text = """
Live Status

Turning ${liveTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow ${liveYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC ${liveFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop ${liveDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Pre-FC ROR ${liveRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()

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
Roast → Correction
Generate Batch 2
            """.trimIndent()
        } else {
            """
Correction Status
Waiting For Data
            """.trimIndent()
        }

        val resetBatchBtn = Button(activity)
        resetBatchBtn.text = "Reset Current Batch"

        val resetAllBtn = Button(activity)
        resetAllBtn.text = "Reset Planner + Batch"

        resetBatchBtn.setOnClickListener {

            AppState.resetBatch()

            show(activity, container)
        }

        resetAllBtn.setOnClickListener {

            AppState.resetAll()

            show(activity, container)
        }

        root.addView(title)
        root.addView(machineCard)
        root.addView(plannerCard)
        root.addView(liveCard)
        root.addView(correctionCard)

        root.addView(resetBatchBtn)
        root.addView(resetAllBtn)

        container.addView(root)
    }
}
