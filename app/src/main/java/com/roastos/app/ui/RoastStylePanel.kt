package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastSessionBus
import com.roastos.app.RoastStyleEngine
import com.roastos.app.UiKit

class RoastStylePanel(
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

        val snapshot =
            RoastSessionBus.peek()

        if (snapshot == null) {

            textView.text =
                """
Style

No roast session running.
                """.trimIndent()

            return
        }

        val profile =
            RoastStyleEngine.recommendForSnapshot(snapshot)

        textView.text =
            RoastStyleEngine.buildDisplayText(profile)
    }
}
