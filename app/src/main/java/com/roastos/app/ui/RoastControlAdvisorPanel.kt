package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastControlAdvisorEngine
import com.roastos.app.RoastSessionBus
import com.roastos.app.UiKit

class RoastControlAdvisorPanel(
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
No active roast session.
            """.trimIndent()
            return
        }

        val output = RoastControlAdvisorEngine.evaluate(snapshot)

        textView.text = """
阶段 / 优先级
${output.stage} / ${output.priority}

执行建议
火力：${output.finalHeatAdvice}
风门：${output.finalAirflowAdvice}

风味方向
${output.flavorDirection}

风险 / 置信度
${output.riskLevel} / ${output.confidence}

系统理解
${output.insightSummary}

参考状态
${output.referenceContextLevel}

参考说明
${output.referenceContext}

原因
${output.reason}
        """.trimIndent()
    }
}
