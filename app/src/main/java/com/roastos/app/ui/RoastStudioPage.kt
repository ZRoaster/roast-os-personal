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

        root.addView(UiKit.pageTitle(context, "ROAST STUDIO"))
        root.addView(UiKit.pageSubtitle(context, "Live Roast Session"))
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        val startBtn = UiKit.primaryButton(context, "START ROAST")
        val stopBtn = UiKit.secondaryButton(context, "STOP ROAST")
        val refreshBtn = UiKit.secondaryButton(context, "REFRESH")

        controlCard.addView(UiKit.cardTitle(context, "CONTROL"))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)

        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val stateCard = UiKit.card(context)
        val stateBody = UiKit.bodyText(context, "")

        stateCard.addView(UiKit.cardTitle(context, "SESSION STATE"))
        stateCard.addView(stateBody)

        root.addView(stateCard)
        root.addView(UiKit.spacer(context))

        val phaseCard = UiKit.card(context)
        val phaseBody = UiKit.bodyText(context, "")

        phaseCard.addView(UiKit.cardTitle(context, "PHASE"))
        phaseCard.addView(phaseBody)

        root.addView(phaseCard)
        root.addView(UiKit.spacer(context))

        val companionCard = UiKit.card(context)
        val companionBody = UiKit.bodyText(context, "")

        companionCard.addView(UiKit.cardTitle(context, "COMPANION"))
        companionCard.addView(companionBody)

        root.addView(companionCard)
        root.addView(UiKit.spacer(context))

        val healthCard = UiKit.card(context)
        val healthBody = UiKit.bodyText(context, "")

        healthCard.addView(UiKit.cardTitle(context, "ROAST HEALTH"))
        healthCard.addView(healthBody)

        root.addView(healthCard)
        root.addView(UiKit.spacer(context))

        val logCard = UiKit.card(context)
        val logBody = UiKit.bodyText(context, "")

        logCard.addView(UiKit.cardTitle(context, "ROAST LOG"))
        logCard.addView(logBody)

        root.addView(logCard)
        root.addView(UiKit.spacer(context))

        val historyCard = UiKit.card(context)
        val historyBody = UiKit.bodyText(context, "")

        historyCard.addView(UiKit.cardTitle(context, "RECENT ROASTS"))
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

            phaseBody.text = buildPhaseText(snapshot)

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

            historyBody.text = buildRecentRoasts(snapshot.recentRoasts)
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

    private fun buildPhaseText(
        snapshot: RoastSessionBusSnapshot
    ): String {
        val phaseState = snapshot.phaseState

        return """
Current
${snapshot.companion.phaseLabel}

Turning Point
${formatPhaseEvent(phaseState.turningPoint)}

Dry End
${formatPhaseEvent(phaseState.dryEnd)}

Maillard Start
${formatPhaseEvent(phaseState.maillardStart)}

First Crack
${formatPhaseEvent(phaseState.firstCrack)}

Drop
${formatPhaseEvent(phaseState.drop)}
        """.trimIndent()
    }

    private fun buildHealthText(
        validation: RoastValidationResult
    ): String {
        if (!validation.hasIssues()) {
            return """
状态
稳定

说明
当前未检测到明显风险。
            """.trimIndent()
        }

        return validation.issues.joinToString("\n\n") {
            """
${it.title}
${it.detail}

等级
${formatRisk(it.severity)}
            """.trimIndent()
        }
    }

    private fun buildRecentRoasts(
        roasts: List<RoastHistoryEntry>
    ): String {
        if (roasts.isEmpty()) {
            return "No roast history yet."
        }

        return roasts.joinToString("\n\n") {
            """
Batch
${it.batchId}

Status
${it.batchStatus}

Health
${it.roastHealthHeadline}

Time
${formatTime(it.createdAtMillis)}
            """.trimIndent()
        }
    }

    private fun formatPhaseEvent(
        event: RoastPhaseEvent?
    ): String {
        if (event == null) return "-"

        return "${formatElapsed(event.elapsedSec)} · ${String.format("%.1f", event.beanTemp)} ℃"
    }

    private fun formatElapsed(sec: Int): String {
        val minutes = sec / 60
        val seconds = sec % 60
        return "%d:%02d".format(minutes, seconds)
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
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
