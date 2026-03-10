package com.roastos.app.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.*

object RoastStudioPage {

    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        val title = UiKit.pageTitle(context, "ROAST STUDIO")
        val subtitle = UiKit.pageSubtitle(context, "Live Roast Session")

        root.addView(title)
        root.addView(subtitle)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        val controlTitle = UiKit.cardTitle(context, "CONTROL")

        val startBtn = UiKit.primaryButton(context, "START ROAST")
        val stopBtn = UiKit.secondaryButton(context, "STOP ROAST")
        val refreshBtn = UiKit.secondaryButton(context, "REFRESH")
        val latestHistoryBtn = UiKit.secondaryButton(context, "OPEN LATEST HISTORY")

        controlCard.addView(controlTitle)
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(latestHistoryBtn)

        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val stateCard = UiKit.card(context)
        val stateTitle = UiKit.cardTitle(context, "SESSION STATE")
        val stateBody = UiKit.bodyText(context, "")

        stateCard.addView(stateTitle)
        stateCard.addView(stateBody)

        root.addView(stateCard)
        root.addView(UiKit.spacer(context))

        val phaseCard = UiKit.card(context)
        val phaseTitle = UiKit.cardTitle(context, "PHASE")
        val phaseBody = UiKit.bodyText(context, "")

        phaseCard.addView(phaseTitle)
        phaseCard.addView(phaseBody)

        root.addView(phaseCard)
        root.addView(UiKit.spacer(context))

        val companionCard = UiKit.card(context)
        val companionTitle = UiKit.cardTitle(context, "COMPANION")
        val companionBody = UiKit.bodyText(context, "")

        companionCard.addView(companionTitle)
        companionCard.addView(companionBody)

        root.addView(companionCard)
        root.addView(UiKit.spacer(context))

        val healthCard = UiKit.card(context)
        val healthTitle = UiKit.cardTitle(context, "ROAST HEALTH")
        val healthBody = UiKit.bodyText(context, "")

        healthCard.addView(healthTitle)
        healthCard.addView(healthBody)

        root.addView(healthCard)
        root.addView(UiKit.spacer(context))

        val logCard = UiKit.card(context)
        val logTitle = UiKit.cardTitle(context, "ROAST LOG")
        val logBody = UiKit.bodyText(context, "")

        logCard.addView(logTitle)
        logCard.addView(logBody)

        root.addView(logCard)
        root.addView(UiKit.spacer(context))

        val historyCard = UiKit.card(context)
        val historyTitle = UiKit.cardTitle(context, "ROAST HISTORY")
        val historyBody = UiKit.bodyText(context, "")

        historyCard.addView(historyTitle)
        historyCard.addView(historyBody)

        root.addView(historyCard)

        fun render() {

            val snapshot = RoastSessionBus.tick()
            val session = snapshot.session

            stateBody.text =
                """
Status
${session.status}

Bean Temp
${String.format("%.1f", session.lastBeanTemp)} ℃

RoR
${String.format("%.1f", session.lastRor)} ℃/min

Elapsed
${formatElapsed(session.lastElapsedSec)}
                """.trimIndent()

            phaseBody.text = snapshot.phaseSummary

            companionBody.text =
                """
${snapshot.companion.title}

${snapshot.companion.body}

Phase
${snapshot.companion.phaseLabel}

Risk
${formatRisk(snapshot.companion.riskLevel)}
                """.trimIndent()

            healthBody.text = buildHealthText(snapshot.validation)

            logBody.text = snapshot.logText
            historyBody.text = RoastHistoryEngine.summary()
        }

        startBtn.setOnClickListener {
            RoastSessionBus.startNewRoast()
            running = true
            render()
        }

        stopBtn.setOnClickListener {
            RoastSessionBus.stopAndSave("HB M2SE")
            running = false
            render()
        }

        refreshBtn.setOnClickListener {
            render()
        }

        latestHistoryBtn.setOnClickListener {
            HistoryDetailPage.show(
                context = context,
                container = container,
                entry = RoastHistoryEngine.latest()
            )
        }

        handler.post(object : Runnable {
            override fun run() {

                if (running) {
                    render()
                }

                handler.postDelayed(this, 1000)
            }
        })

        render()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildHealthText(
        validation: RoastValidationResult
    ): String {
        if (!validation.hasIssues()) {
            return """
状态
稳定

等级
无

说明
当前未检测到明显风险。
            """.trimIndent()
        }

        val issues = validation.issues.joinToString("\n\n") { issue ->
            """
${issue.title}
${issue.detail}
等级：${formatRisk(issue.severity)}
            """.trimIndent()
        }

        return """
状态
存在风险

等级
${formatRisk(validation.highestSeverity())}

$issues
        """.trimIndent()
    }

    private fun formatElapsed(sec: Int): String {

        val minutes = sec / 60
        val seconds = sec % 60

        return "%d:%02d".format(minutes, seconds)
    }

    private fun formatRisk(risk: String): String {
        return when (risk) {
            "none" -> "无"
            "low" -> "低"
            "watch" -> "留意"
            "medium" -> "中"
            "high" -> "高"
            else -> risk
        }
    }
}
