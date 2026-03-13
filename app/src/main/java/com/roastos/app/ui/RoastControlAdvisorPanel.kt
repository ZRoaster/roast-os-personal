package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastControlAdvisorEngine
import com.roastos.app.RoastSessionBus
import com.roastos.app.UiKit

class RoastControlAdvisorPanel(
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
Control Advisor

No active roast session.
            """.trimIndent()
            return
        }

        val output = RoastControlAdvisorEngine.evaluate(snapshot)
        textView.text = output.summaryText()
    }
}
