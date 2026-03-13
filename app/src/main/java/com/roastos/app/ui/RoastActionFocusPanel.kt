package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastControlAdvisorEngine
import com.roastos.app.RoastSessionBus
import com.roastos.app.UiKit

class RoastActionFocusPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        val snapshot = RoastSessionBus.peek()

        if (snapshot == null) {
            textView.text = """
ACTION FOCUS

暂无进行中的烘焙
            """.trimIndent()
            return
        }

        val advisor = RoastControlAdvisorEngine.evaluate(snapshot)

        textView.text = """
当前重点
${advisor.priority}

火力动作
${advisor.finalHeatAdvice}

风门动作
${advisor.finalAirflowAdvice}

风险
${advisor.riskLevel}
        """.trimIndent()
    }
}
