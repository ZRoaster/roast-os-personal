package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastCompanionEngine
import com.roastos.app.RoastSessionEngine
import com.roastos.app.UiKit

class RoastInsightPanel(context: Context) : LinearLayout(context) {

    private val companionBody = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(companionBody)
        update()
    }

    fun update() {
        val session = RoastSessionEngine.currentState()
        companionBody.text = RoastCompanionEngine.buildDisplayText(session)
    }
}
