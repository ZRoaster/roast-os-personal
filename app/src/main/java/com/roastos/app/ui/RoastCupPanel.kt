package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastCupProfileEngine
import com.roastos.app.RoastLogEngine
import com.roastos.app.RoastSessionEngine
import com.roastos.app.UiKit

class RoastCupPanel(context: Context) : LinearLayout(context) {

    private val bodyText = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(bodyText)
        update()
    }

    fun update() {
        val session = RoastSessionEngine.currentState()
        val log = RoastLogEngine.buildLog(session)
        val cup = RoastCupProfileEngine.evaluate(log)

        bodyText.text =
            """
风味预测
${cup.flavorPrediction}

推荐冲煮
${cup.brewMethod}

建议水温
${cup.brewTempC} ℃

建议粉水比
${cup.brewRatio}

建议研磨
${cup.grindLevel}

说明
${cup.brewNote}
            """.trimIndent()
    }
}
