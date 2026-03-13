package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastCalibrationMatcherEngine
import com.roastos.app.RoastControlAdvisorEngine
import com.roastos.app.RoastRorPredictionEngine
import com.roastos.app.RoastSessionBus
import com.roastos.app.UiKit

class RoastExecutiveSummaryPanel(
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
Executive Summary

No active roast session.
            """.trimIndent()
            return
        }

        val advisor = RoastControlAdvisorEngine.evaluate(snapshot)
        val prediction = RoastRorPredictionEngine.evaluate(snapshot)
        val match = RoastCalibrationMatcherEngine.matchBest()

        val fcText = prediction.estimatedFirstCrackWindowSec?.let {
            formatSec(it)
        } ?: "-"

        val calibrationId = match.matchedProfile?.calibrationId ?: "manual/default"

        textView.text = """
当前阶段
${advisor.stage}

当前重点
${advisor.priority}

最终火力建议
${advisor.finalHeatAdvice}

最终风门建议
${advisor.finalAirflowAdvice}

预测风险
${advisor.riskLevel}

预计一爆时间
$fcText

当前标定基线
$calibrationId
        """.trimIndent()
    }

    private fun formatSec(value: Int): String {
        val m = value / 60
        val s = value % 60
        return "%d:%02d".format(m, s)
    }
}
