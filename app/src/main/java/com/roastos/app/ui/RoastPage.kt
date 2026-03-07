package com.roastos.app.ui

import android.app.Activity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object RoastPage {

    fun show(activity: Activity, container: LinearLayout) {

        container.removeAllViews()

        val title = TextView(activity)
        title.text = "Roast Center"
        title.textSize = 22f

        container.addView(title)

        val planner = Button(activity)
        planner.text = "Planner"

        val live = Button(activity)
        live.text = "Live Assist"

        val correction = Button(activity)
        correction.text = "Batch Correction"

        container.addView(planner)
        container.addView(live)
        container.addView(correction)

        val area = LinearLayout(activity)
        area.orientation = LinearLayout.VERTICAL

        container.addView(area)

        planner.setOnClickListener {
            area.removeAllViews()
            val t = TextView(activity)
            t.text = "Planner page"
            area.addView(t)
        }

        live.setOnClickListener {
            area.removeAllViews()
            val t = TextView(activity)
            t.text = "Live Assist page"
            area.addView(t)
        }

        correction.setOnClickListener {
            area.removeAllViews()
            val t = TextView(activity)
            t.text = "Batch Correction page"
            area.addView(t)
        }
    }
}
