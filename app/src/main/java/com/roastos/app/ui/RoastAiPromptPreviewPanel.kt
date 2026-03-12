package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastAiPromptBuilder
import com.roastos.app.RoastAiRealtimeContextBuilder
import com.roastos.app.UiKit

class RoastAiPromptPreviewPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        val aiContext = RoastAiRealtimeContextBuilder.build(
            userPrompt = "Preview current AI prompt"
        )

        val prompt = RoastAiPromptBuilder.buildFullPrompt(aiContext)

        textView.text = """
AI Prompt Preview

$prompt
        """.trimIndent()
    }
}
