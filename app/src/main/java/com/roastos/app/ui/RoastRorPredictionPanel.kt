package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastRorPredictionEngine
import com.roastos.app.RoastSessionBus
import com.roastos.app.UiKit

class RoastRorPredictionPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        val snapshot = RoastSessionBus.peek()

        if (snapshot == null) {
            textView.text = """
RoR Prediction

No active roast session.
            """.trimIndent()
            return
        }

        val prediction = RoastRorPredictionEngine.evaluate(snapshot)
        textView.text = prediction.summaryText()
    }
}
