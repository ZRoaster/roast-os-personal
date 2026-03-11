package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastKnowledgeEngine
import com.roastos.app.UiKit

class RoastKnowledgePanel(
    context: Context
) : LinearLayout(context) {

    private val textView =
        UiKit.bodyText(context, "")

    init {

        orientation = VERTICAL

        addView(textView)

        update()
    }

    fun update() {

        val summary =
            RoastKnowledgeEngine.summary()

        textView.text =
            """
Roast Knowledge Summary

$summary
            """.trimIndent()
    }
}
