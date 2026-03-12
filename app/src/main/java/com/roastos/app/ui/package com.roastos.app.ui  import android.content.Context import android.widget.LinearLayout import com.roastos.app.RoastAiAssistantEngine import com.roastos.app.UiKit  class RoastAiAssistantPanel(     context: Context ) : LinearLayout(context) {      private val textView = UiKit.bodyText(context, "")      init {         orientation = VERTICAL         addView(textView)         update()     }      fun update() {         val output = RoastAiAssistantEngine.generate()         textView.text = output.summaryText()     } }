package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastAiAssistantEngine
import com.roastos.app.UiKit

class RoastAiAssistantPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        val output = RoastAiAssistantEngine.generate()
        textView.text = output.summaryText()
    }
}
