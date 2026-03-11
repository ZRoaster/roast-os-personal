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

        val overviewCard = UiKit.card(context)
        val overviewBody = UiKit.bodyText(context, "")

        overviewCard.addView(UiKit.cardTitle(context, "ROAST STATUS"))
        overviewCard.addView(overviewBody)

        root.addView(overviewCard)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)

        val startBtn = UiKit.primaryButton(context, "START ROAST")
        val stopBtn = UiKit.secondaryButton(context, "STOP ROAST")
        val refreshBtn = UiKit.secondaryButton(context, "REFRESH")
        val openRecentBtn = UiKit.secondaryButton(context, "OPEN RECENT ROASTS")

        controlCard.addView(UiKit.cardTitle(context, "CONTROL"))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(openRecentBtn)

        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val healthCard = UiKit.card(context)
        val healthBody = UiKit.bodyText(context, "")

        healthCard.addView(UiKit.cardTitle(context, "ROAST HEALTH"))
        healthCard.addView(healthBody)

        root.addView(healthCard)
        root.addView(UiKit.spacer(context))

        val decisionCard = UiKit.card(context)
        val decisionBody = UiKit.bodyText(context, "")

        decisionCard.addView(UiKit.cardTitle(context, "DECISION"))
        decisionCard.addView(decisionBody)

        root.addView(decisionCard)
        root.addView(UiKit.spacer(context))

        val companionCard = UiKit.card(context)
        val companionBody = UiKit.bodyText(context, "")

        companionCard.addView(UiKit.cardTitle(context, "COMPANION"))
        companionCard.addView(companionBody)

        root.addView(companionCard)
        root.addView(UiKit.spacer(context))

        val phaseCard = UiKit.card(context)
        val phaseBody = UiKit.bodyText(context, "")

        phaseCard.addView(UiKit.cardTitle(context, "PHASE"))
        phaseCard.addView(phaseBody)

        root.addView(phaseCard)
        root.addView(UiKit.spacer(context))

        val logCard = UiKit.card(context)
        val logBody = UiKit.bodyText(context, "")

        logCard.addView(UiKit.cardTitle(context, "ROAST LOG"))
        logCard.addView(logBody)

        root.addView(logCard)
        root.addView(UiKit.spacer(context))

        val historyCard = UiKit.card(context)
        val historyBody = UiKit.bodyText(context, "")
        val openLatestBtn = UiKit.secondaryButton(context, "OPEN LATEST HISTORY")

        historyCard.addView(UiKit.cardTitle(context, "RECENT ROASTS"))
        historyCard.addView(historyBody)
        historyCard.addView(UiKit.spacer(context))
        historyCard.addView(openLatestBtn)

        root.addView(historyCard)

        fun render() {

            val snapshot = RoastSessionBus.tick()
            val session = snapshot.session
            val decision = RoastDecisionEngine.evaluate(snapshot)

            overviewBody.text =
                """
STATUS   ${session.status}

BT       ${String.format("%.1f", session.lastBeanTemp)} ℃

RoR      ${String.format("%.1f", session.lastRor)} ℃/min

TIME     ${formatElapsed(session.lastElapsedSec)}

HEALTH   ${buildHealthHeadline(snapshot.validation)}
                """.trimIndent()

            healthBody.text = buildHealthText(snapshot.validation)

            decisionBody.text = buildDecisionPanel(decision)

            companionBody.text =
                """
${snapshot.companion.title}

${snapshot.companion.body}

PHASE
${snapshot.companion.phaseLabel}

RISK
${formatRisk(snapshot.companion.riskLevel)}
                """.trimIndent()

            phaseBody.text = buildPhasePanel(snapshot)

            logBody.text = buildLog(snapshot.log)

            historyBody.text = buildRecent(snapshot.recentRoasts)
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

        openRecentBtn.setOnClickListener {
            RecentRoastListPage.show(context, container)
        }

        openLatestBtn.setOnClickListener {
            HistoryDetailPage.show(
                context,
                container,
                RoastHistoryEngine.latest()
            )
        }

        handler.post(object : Runnable {
            override fun run() {
                if (running) render()
                handler.postDelayed(this, 1000)
            }
        })

        render()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildDecisionPanel(
        decision: RoastDecision
    ): String {
        return """
阶段
${decision.stage}

当前重点
${decision.priority}

火力建议
${decision.heatAction}

风门建议
${decision.airflowAction}

风味走向
${decision.flavorDirection}

可信度
${decision.confidence}

判断依据
${decision.rationale}
        """.trimIndent()
    }

    private fun buildPhasePanel(snapshot: RoastSessionBusSnapshot): String {

        val p = snapshot.phaseState

        return """
CURRENT
${snapshot.companion.phaseLabel}

TURNING   ${formatPhase(p.turningPoint)}
DRY END   ${formatPhase(p.dryEnd)}
MAILLARD  ${formatPhase(p.maillardStart)}
FC        ${formatPhase(p.firstCrack)}
DROP      ${formatPhase(p.drop)}
        """.trimIndent()
    }

    private fun buildLog(log: RoastLog): String {

        return """
BATCH
${log.batchId}

STATUS
${log.status}

TOTAL TIME
${formatElapsed(log.totalTimeSec)}

FC
${formatEvent(log.firstCrackSec, log.firstCrackTemp)}

DROP
${formatEvent(log.dropSec, log.dropTemp)}

DEV RATIO
${formatRatio(log.developmentRatio)}

FINAL ROR
${formatRor(log.finalRor)}
        """.trimIndent()
    }

    private fun buildRecent(list: List<RoastHistoryEntry>): String {

        if (list.isEmpty()) return "No roast history yet."

        return list.joinToString("\n\n────────\n\n") {
            """
BATCH
${it.batchId}

STATUS
${it.batchStatus}

HEALTH
${it.roastHealthHeadline}

TIME
${formatTime(it.createdAtMillis)}
            """.trimIndent()
        }
    }

    private fun buildHealthHeadline(v: RoastValidationResult): String {

        if (!v.hasIssues()) return "稳定"

        return when (v.highestSeverity()) {
            "high" -> "高风险"
            "medium" -> "中风险"
            "watch" -> "需留意"
            "low" -> "低风险"
            else -> "稳定"
        }
    }

    private fun buildHealthText(v: RoastValidationResult): String {

        if (!v.hasIssues()) {
            return """
状态
稳定

说明
当前未检测到明显风险
            """.trimIndent()
        }

        return v.issues.joinToString("\n\n") {
            """
${it.title}
${it.detail}

等级
${formatRisk(it.severity)}
            """.trimIndent()
        }
    }

    private fun formatPhase(e: RoastPhaseEvent?): String {
        if (e == null) return "-"
        return "${formatElapsed(e.elapsedSec)} · ${String.format("%.1f", e.beanTemp)}℃"
    }

    private fun formatEvent(sec: Int?, temp: Double?): String {
        if (sec == null || temp == null) return "-"
        return "${formatElapsed(sec)} · ${String.format("%.1f", temp)}℃"
    }

    private fun formatElapsed(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatTime(ms: Long): String {
        val t = ms / 1000
        val m = t / 60
        val s = t % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatRatio(v: Double?): String {
        if (v == null) return "-"
        return "${String.format("%.1f", v * 100)}%"
    }

    private fun formatRor(v: Double?): String {
        if (v == null) return "-"
        return "${String.format("%.1f", v)} ℃/min"
    }

    private fun formatRisk(r: String): String {
        return when (r) {
            "none" -> "无"
            "low" -> "低"
            "watch" -> "留意"
            "medium" -> "中"
            "high" -> "高"
            else -> r
        }
    }
}
