package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastLearningEngine
import com.roastos.app.UiKit

class RoastLearningPanel(context: Context) : LinearLayout(context) {

    private val bodyText = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(bodyText)
        update()
    }

    fun update() {
        bodyText.text = RoastLearningEngine.latestLearningText()
    }
}
