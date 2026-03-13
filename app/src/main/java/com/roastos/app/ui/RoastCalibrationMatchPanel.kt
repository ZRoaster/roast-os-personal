package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastCalibrationMatcherEngine
import com.roastos.app.UiKit

class RoastCalibrationMatchPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        val result = RoastCalibrationMatcherEngine.matchBest()
        textView.text = result.summaryText()
    }
}
