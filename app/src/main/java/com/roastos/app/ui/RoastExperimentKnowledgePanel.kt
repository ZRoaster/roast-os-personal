package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastExperimentKnowledgeEngine
import com.roastos.app.UiKit

class RoastExperimentKnowledgePanel(
    context: Context
) : LinearLayout(context) {

    private val body = UiKit.bodyText(context, "")

    init {

        orientation = VERTICAL

        addView(body)

        update()
    }

    fun update() {

        body.text = RoastExperimentKnowledgeEngine.latestText()

    }
}
