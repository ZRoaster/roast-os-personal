package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastCalibrationMatcherEngine
import com.roastos.app.RoastControlAdvisorEngine
import com.roastos.app.RoastInsightBridge
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
No active roast session.
            """.trimIndent()
            return
        }

        val advisor = RoastControlAdvisorEngine.evaluate(snapshot)
        val prediction = RoastRorPredictionEngine.evaluate(snapshot)
        val match = RoastCalibrationMatcherEngine.matchBest()
        val observationHeadline = RoastInsightBridge.observationHeadlineForSnapshot(snapshot)

        val fcText = prediction.estimatedFirstCrackWindowSec?.let {
            formatSec(it)
        } ?: "-"

        val calibrationId = match.matchedProfile?.calibrationId ?: "manual/default"

        textView.text = """
观察
$observationHeadline

阶段
${advisor.stage}

重点
${advisor.priority}

系统理解
${advisor.insightSummary}

建议
火力：${advisor.finalHeatAdvice}
风门：${advisor.finalAirflowAdvice}

风险 / 一爆
${advisor.riskLevel} / $fcText

基线
$calibrationId
        """.trimIndent()
    }

    private fun formatSec(value: Int): String {
        val m = value / 60
        val s = value % 60
        return "%d:%02d".format(m, s)
    }
}
