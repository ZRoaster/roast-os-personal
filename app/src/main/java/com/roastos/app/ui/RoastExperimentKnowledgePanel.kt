package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastExperimentKnowledgeEngine
import com.roastos.app.UiKit

class RoastExperimentKnowledgePanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        textView.text = RoastExperimentKnowledgeEngine.latestText()
    }
}
