package com.roastos.app.ui

import android.app.Activity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object RoastPage {

    fun show(activity: Activity, container: LinearLayout) {

        container.removeAllViews()

        val root = LinearLayout(activity)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(activity)
        title.text = "ROAST CONTROL"
        title.textSize = 22f

        val subtitle = TextView(activity)
        subtitle.text = "Planner → Live Assist → Correction"

        val plannerCard = TextView(activity)
        plannerCard.text = """
Planner

Generate roast baseline
Calculate charge / FC / drop prediction
Build heat and airflow plan
        """.trimIndent()

        val plannerBtn = Button(activity)
        plannerBtn.text = "Open Planner"

        plannerBtn.setOnClickListener {
            PlannerPage.show(activity, container)
        }

        val liveCard = TextView(activity)
        liveCard.text = """
Live Assist

Record Turning / Yellow / FC
Get real-time heat and airflow advice
Phase detection + curve prediction
        """.trimIndent()

        val liveBtn = Button(activity)
        liveBtn.text = "Open Live Assist"

        liveBtn.setOnClickListener {
            LiveAssistPage.show(activity, container)
        }

        val correctionCard = TextView(activity)
        correctionCard.text = """
Batch Correction

Compare predicted vs actual anchors
Diagnose roast deviation
Generate Batch-2 execution card
        """.trimIndent()

        val correctionBtn = Button(activity)
        correctionBtn.text = "Open Correction"

        correctionBtn.setOnClickListener {
            CorrectionPage.show(activity, container)
        }

        root.addView(title)
        root.addView(subtitle)

        root.addView(plannerCard)
        root.addView(plannerBtn)

        root.addView(liveCard)
        root.addView(liveBtn)

        root.addView(correctionCard)
        root.addView(correctionBtn)

        container.addView(root)
    }
}
