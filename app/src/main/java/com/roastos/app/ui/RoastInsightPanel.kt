package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastPhaseDetectionEngine
import com.roastos.app.RoastSessionEngine
import com.roastos.app.UiKit

class RoastInsightPanel(context: Context) : LinearLayout(context) {

    private val insightText = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
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
                beanTemp < 120 -> "Bean is still absorbing heat."
                beanTemp < 160 -> "Drying phase is building steadily."
                beanTemp < 190 -> "Maillard activity is increasing."
                else -> "Roast is entering development."
            }

        val momentumText =
            when {
                momentum > 12 -> "RoR is strong."
                momentum > 7 -> "RoR is stable."
                momentum > 3 -> "RoR is easing down."
                else -> "RoR is very low."
            }

        val suggestion =
            when {
                momentum > 12 -> "Reduce heat slightly if needed."
                momentum > 7 -> "Hold current energy."
                momentum > 3 -> "Watch momentum carefully."
                else -> "Add a little energy to avoid stall."
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
