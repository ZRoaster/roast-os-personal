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

            phaseBody.text = snapshot.phaseSummary

            companionBody.text =
                """
${snapshot.companion.title}

${snapshot.companion.body}

Phase
${snapshot.companion.phaseLabel}

Risk
${snapshot.companion.riskLevel}
                """.trimIndent()

            healthBody.text = buildHealthText(snapshot.validation)

            logBody.text = snapshot.logText

            historyBody.text = buildRecentRoasts()
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

    private fun buildRecentRoasts(): String {

        val list = RoastHistoryEngine.all().take(3)

        if (list.isEmpty()) {
            return "No roast history yet."
        }

        return list.joinToString("\n\n") {

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

    private fun buildHealthText(validation: RoastValidationResult): String {

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
${it.severity}
            """.trimIndent()
        }
    }

    private fun formatElapsed(sec: Int): String {

        val minutes = sec / 60
        val seconds = sec % 60

        return "%d:%02d".format(minutes, seconds)
    }

    private fun formatTime(ms: Long): String {

        val seconds = ms / 1000
        val minutes = seconds / 60
        val remain = seconds % 60

        return "%d:%02d".format(minutes, remain)
    }
}
