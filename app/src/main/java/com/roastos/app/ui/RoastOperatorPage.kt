package com.roastos.app.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.*

object RoastOperatorPage {

    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST OPERATOR"))
        root.addView(UiKit.pageSubtitle(context, "Primary Action View"))
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(UiKit.cardTitle(context, "ROAST STATUS"))
        statusCard.addView(statusBody)
        root.addView(statusCard)
        root.addView(UiKit.spacer(context))

        val executiveCard = UiKit.card(context)
        val executivePanel = RoastExecutiveSummaryPanel(context)
        executiveCard.addView(UiKit.cardTitle(context, "EXECUTIVE SUMMARY"))
        executiveCard.addView(executivePanel)
        root.addView(executiveCard)
        root.addView(UiKit.spacer(context))

        val advisorCard = UiKit.card(context)
        val advisorPanel = RoastControlAdvisorPanel(context)
        advisorCard.addView(UiKit.cardTitle(context, "CONTROL ADVISOR"))
        advisorCard.addView(advisorPanel)
        root.addView(advisorCard)
        root.addView(UiKit.spacer(context))

        val predictionCard = UiKit.card(context)
        val predictionPanel = RoastRorPredictionPanel(context)
        predictionCard.addView(UiKit.cardTitle(context, "ROR PREDICTION"))
        predictionCard.addView(predictionPanel)
        root.addView(predictionCard)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        val refreshBtn = UiKit.secondaryButton(context, "REFRESH")
        val openStudioBtn = UiKit.secondaryButton(context, "OPEN STUDIO")
        val openEnvironmentBtn = UiKit.secondaryButton(context, "OPEN ENVIRONMENT")

        controlCard.addView(UiKit.cardTitle(context, "QUICK CONTROL"))
        controlCard.addView(refreshBtn)
        controlCard.addView(openStudioBtn)
        controlCard.addView(openEnvironmentBtn)
        root.addView(controlCard)

        fun render() {
            val snapshot = RoastSessionBus.tick()
            val session = snapshot.session

            statusBody.text =
                """
STATUS   ${session.status}

BT       ${String.format("%.1f", session.lastBeanTemp)} ℃

RoR      ${String.format("%.1f", session.lastRor)} ℃/min

TIME     ${formatElapsed(session.lastElapsedSec)}

PHASE    ${snapshot.companion.phaseLabel}

HEALTH   ${buildHealthHeadline(snapshot.validation)}
                """.trimIndent()

            executivePanel.update()
            advisorPanel.update()
            predictionPanel.update()
        }

        refreshBtn.setOnClickListener {
            render()
        }

        openStudioBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        openEnvironmentBtn.setOnClickListener {
            EnvironmentInputPage.show(context, container)
        }

        handler.post(object : Runnable {
            override fun run() {
                if (running) {
                    render()
                }
                handler.postDelayed(this, 1000)
            }
        })

        running = true
        render()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildHealthHeadline(
        validation: RoastValidationResult
    ): String {
        if (!validation.hasIssues()) return "稳定"

        return when (validation.highestSeverity()) {
            "high" -> "高风险"
            "medium" -> "中风险"
            "watch" -> "需留意"
            "low" -> "低风险"
            else -> "稳定"
        }
    }

    private fun formatElapsed(
        sec: Int
    ): String {
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }
}
