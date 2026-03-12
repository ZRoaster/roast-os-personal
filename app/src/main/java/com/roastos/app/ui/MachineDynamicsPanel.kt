package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.MachineDynamicsEngine
import com.roastos.app.UiKit

class MachineDynamicsPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        textView.text = MachineDynamicsEngine.adjustedSummary()
    }
}
