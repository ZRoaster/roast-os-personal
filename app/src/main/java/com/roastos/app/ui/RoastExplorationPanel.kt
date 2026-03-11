package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastExplorationEngine
import com.roastos.app.RoastSessionBus
import com.roastos.app.UiKit

class RoastExplorationPanel(context: Context) : LinearLayout(context) {

    private val bodyText = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(bodyText)
        update()
    }

    fun update() {
        val snapshot = RoastSessionBus.current()

        if (snapshot == null) {
            bodyText.text = """
Exploration

No active session snapshot.
            """.trimIndent()
            return
        }

        val batchId = snapshot.log.batchId
        bodyText.text = RoastExplorationEngine.buildDisplayText(batchId)
    }
}
