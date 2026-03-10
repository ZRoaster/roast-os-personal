package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.*

class RoastInsightPanel(context: Context) : LinearLayout(context) {

    private val insightText = UiKit.bodyText(context, "")

    init {

        orientation = VERTICAL

        val title = UiKit.cardTitle(context, "ROAST INSIGHT")

        addView(title)
        addView(UiKit.spacer(context))
        addView(insightText)

        update()
    }

    fun update() {

        val session = RoastSessionEngine.currentState()

        val phaseSummary = RoastPhaseDetectionEngine.summary()

        val momentum = session.lastRor
        val beanTemp = session.lastBeanTemp
        val elapsed = session.lastElapsedSec

        val observation =
            when {
                beanTemp < 120 -> "Bean still in early thermal absorption."
                beanTemp < 160 -> "Drying phase energy transfer ongoing."
                beanTemp < 190 -> "Maillard reactions increasing."
                else -> "Approaching development phase."
            }

        val momentumText =
            when {
                momentum > 12 -> "RoR momentum strong."
                momentum > 7 -> "RoR stable."
                momentum > 3 -> "RoR momentum decreasing."
                else -> "RoR very low."
            }

        val suggestion =
            when {
                momentum > 12 -> "Consider reducing heat slightly."
                momentum > 7 -> "Maintain current energy input."
                momentum > 3 -> "Monitor development carefully."
                else -> "Increase heat slightly to avoid stall."
            }

        insightText.text =
            """
Observation
$observation

RoR State
$momentumText

Suggestion
$suggestion

Phase Engine
$phaseSummary

Elapsed
${formatTime(elapsed)}
            """.trimIndent()
    }

    private fun formatTime(sec: Int): String {

        val m = sec / 60
        val s = sec % 60

        return "%d:%02d".format(m, s)
    }
}
