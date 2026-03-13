package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastAiSessionEngine
import com.roastos.app.UiKit

class RoastAiContextPreviewPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        val session = RoastAiSessionEngine.current()
            ?: RoastAiSessionEngine.build(
                userPrompt = "Preview current AI context"
            )

        textView.text = session.context.summary()
    }
}
