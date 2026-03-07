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

        val tabs = LinearLayout(activity)
        tabs.orientation = LinearLayout.HORIZONTAL

        val plannerBtn = Button(activity)
        plannerBtn.text = "Planner"

        val liveBtn = Button(activity)
        liveBtn.text = "Live"

        val correctionBtn = Button(activity)
        correctionBtn.text = "Correction"

        tabs.addView(plannerBtn)
        tabs.addView(liveBtn)
        tabs.addView(correctionBtn)

        container.addView(tabs)

        val sub = LinearLayout(activity)
        sub.orientation = LinearLayout.VERTICAL
        container.addView(sub)

        plannerBtn.setOnClickListener {
            sub.removeAllViews()
            val t = TextView(activity)
            t.text = "Planner page placeholder"
            sub.addView(t)
        }

        liveBtn.setOnClickListener {
            sub.removeAllViews()
            val t = TextView(activity)
            t.text = "Live Assist page placeholder"
            sub.addView(t)
        }

        correctionBtn.setOnClickListener {
            sub.removeAllViews()
            val t = TextView(activity)
            t.text = "Batch Correction page placeholder"
            sub.addView(t)
        }

        val first = TextView(activity)
        first.text = "Planner page placeholder"
        sub.addView(first)
    }
}
