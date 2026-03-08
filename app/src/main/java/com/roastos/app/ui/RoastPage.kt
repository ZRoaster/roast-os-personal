package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

object RoastPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val inner = LinearLayout(context)
        inner.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "ROAST"
        title.textSize = 22f

        val content = TextView(context)
        content.text = LiveAssistPage.buildLiveAssist()

        inner.addView(title)
        inner.addView(content)

        scroll.addView(inner)
        container.addView(scroll)
    }
}
