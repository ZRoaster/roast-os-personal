package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.EnvironmentProfileEngine
import com.roastos.app.UiKit

class EnvironmentProfilePanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        textView.text = EnvironmentProfileEngine.summary()
    }
}
