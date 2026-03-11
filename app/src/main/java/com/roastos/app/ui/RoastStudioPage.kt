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

        controlCard.addView(UiKit.cardTitle(context, "CONTROL"))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)

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

        // -------- CUP PROFILE 面板 --------

        val cupCard = UiKit.card(context)
        val cupBody = UiKit.bodyText(context, "")

        cupCard.addView(UiKit.cardTitle(context, "CUP PROFILE"))
        cupCard.addView(cupBody)

        root.addView(cupCard)
        root.addView(UiKit.spacer(context))

        val historyCard = UiKit.card(context)
        val historyBody = UiKit.bodyText(context, "")

        historyCard.addView(UiKit.cardTitle(context, "RECENT ROASTS"))
        historyCard.addView(historyBody)

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
                """.trimIndent()

            healthBody.text = buildHealthText(snapshot.validation)

            decisionBody.text =
                """
阶段
${decision.stage}

重点
${decision.priority}

火力
${decision.heatAction}

风门
${decision.airflowAction}

可信度
${decision.confidence}
                """.trimIndent()

            companionBody.text =
                """
${snapshot.companion.title}

${snapshot.companion.body}
                """.trimIndent()

            phaseBody.text = snapshot.companion.phaseLabel

            logBody.text = snapshot.log.summary()

            // ------- CUP PROFILE -------
            val cup = RoastCupProfileEngine.evaluate(snapshot.log)

            cupBody.text =
                """
风味预测
${cup.flavorPrediction}

推荐冲煮
${cup.brewMethod}

水温
${cup.brewTempC} ℃

粉水比
${cup.brewRatio}

研磨
${cup.grindLevel}

说明
${cup.brewNote}
                """.trimIndent()

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

    private fun buildHealthText(v: RoastValidationResult): String {

        if (!v.hasIssues()) {
            return "当前未检测到明显风险"
        }

        return v.issues.joinToString("\n\n") {
            "${it.title}\n${it.detail}"
        }
    }

    private fun formatElapsed(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }
}
