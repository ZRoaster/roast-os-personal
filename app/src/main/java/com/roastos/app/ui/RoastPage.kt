package com.roastos.app.ui

import android.content.Context
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

object RoastPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 24, 24, 24)

        val title = TextView(context)
        title.text = "ROAST CENTER"
        title.textSize = 24f
        title.setTypeface(null, Typeface.BOLD)

        val subtitle = TextView(context)
        subtitle.text = "Live assist, timeline tracking, and roast control analysis"
        subtitle.textSize = 14f

        val content = TextView(context)
        content.text = LiveAssistPage.buildLiveAssist()
        content.textSize = 15f
        content.setPadding(0, 24, 0, 24)

        root.addView(title)
        root.addView(subtitle)
        root.addView(content)

        scroll.addView(root)
        container.addView(scroll)
    }
}
