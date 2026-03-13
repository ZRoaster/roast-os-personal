package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastControlModel
import com.roastos.app.UiKit

class RoastControlModelPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        val adjustment = RoastControlModel.evaluate()
        textView.text = adjustment.summaryText()
    }
}
