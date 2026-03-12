package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.EnvironmentCompensationEngine
import com.roastos.app.EnvironmentProfileEngine
import com.roastos.app.RoastAiContexts
import com.roastos.app.RoastAiIntentType
import com.roastos.app.RoastSessionBus

class RoastAiContextPreviewPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = TextView(context)

    init {
        orientation = VERTICAL
        textView.textSize = 12f
        addView(textView)
        update()
    }

    fun update() {
        val snapshot = RoastSessionBus.peek()

        if (snapshot == null) {
            textView.text = """
AI Context Preview

No active roast session.
            """.trimIndent()
            return
        }

        val contextPreview = RoastAiContexts.buildMinimal(
            intent = RoastAiIntentType.REALTIME_COACHING,
            userPrompt = "AI Context Preview",
            environmentProfile = EnvironmentProfileEngine.current(),
            environmentCompensation = EnvironmentCompensationEngine.evaluate()
        )

        textView.text = contextPreview.summary()
    }
}
