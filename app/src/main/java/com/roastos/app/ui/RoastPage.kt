package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object RoastPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "ROAST CENTER"
        title.textSize = 22f

        val plannerBtn = Button(context)
        plannerBtn.text = "Planner"

        val liveBtn = Button(context)
        liveBtn.text = "Live Assist"

        val correctionBtn = Button(context)
        correctionBtn.text = "Correction"

        val content = LinearLayout(context)
        content.orientation = LinearLayout.VERTICAL

        root.addView(title)
        root.addView(plannerBtn)
        root.addView(liveBtn)
        root.addView(correctionBtn)
        root.addView(content)

        container.addView(root)

        plannerBtn.setOnClickListener {
            PlannerPage.show(context, content)
        }

        liveBtn.setOnClickListener {
            LiveAssistPage.show(context, content)
        }

        correctionBtn.setOnClickListener {
            content.removeAllViews()

            val text = TextView(context)
            text.text = "Correction Engine Ready"

            content.addView(text)
        }

        PlannerPage.show(context, content)
    }
}
