package com.roastos.app.ui

import android.app.Activity
import android.widget.LinearLayout
import android.widget.TextView

object DashboardPage {

    fun show(activity: Activity, container: LinearLayout) {

        container.removeAllViews()

        val title = TextView(activity)
        title.text = "Roast OS"
        title.textSize = 22f

        val info = TextView(activity)
        info.text = """
Machine: HB M2SE
Batch: 200g
Charge: 204℃
Max Power: 1450W

Planner → RoastEngine
Correction → CorrectionEngine
Live Assist → LiveAssistEngine
""".trimIndent()

        container.addView(title)
        container.addView(info)
    }
}
