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
        val openLatestBtn = UiKit.secondaryButton(context, "OPEN LATEST HISTORY")
        val openKnowledgeBtn = UiKit.secondaryButton(context, "OPEN KNOWLEDGE")
        val openStylesBtn = UiKit.secondaryButton(context, "OPEN STYLES")
        val openMyStylesBtn = UiKit.secondaryButton(context, "OPEN MY STYLES")

        controlCard.addView(UiKit.cardTitle(context, "CONTROL"))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(openRecentBtn)
        controlCard.addView(openLatestBtn)
        controlCard.addView(openKnowledgeBtn)
        controlCard.addView(openStylesBtn)
        controlCard.addView(openMyStylesBtn)

        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val styleCard = UiKit.card(context)
        val stylePanel = RoastStylePanel(context)

        styleCard.addView(UiKit.cardTitle(context, "STYLE"))
        styleCard.addView(stylePanel)

        root.addView(styleCard)
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
STATUS
${session.status}

BT
${String.format("%.1f", session.lastBeanTemp)} ℃

RoR
${String.format("%.1f", session.lastRor)} ℃/min

TIME
${formatElapsed(session.lastElapsedSec)}
                """.trimIndent()

            decisionBody.text =
                """
Stage
${decision.stage}

Priority
${decision.priority}

Heat
${decision.heatAction}

Airflow
${decision.airflowAction}

Flavor
${decision.flavorDirection}
                """.trimIndent()

            companionBody.text =
                """
${snapshot.companion.title}

${snapshot.companion.body}
                """.trimIndent()

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

        openKnowledgeBtn.setOnClickListener {
            RoastKnowledgePage.show(context, container)
        }

        openStylesBtn.setOnClickListener {
            RoastStylePage.show(context, container)
        }

        openMyStylesBtn.setOnClickListener {
            MyStylePage.show(context, container)
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

    private fun buildRecent(list: List<RoastHistoryEntry>): String {

        if (list.isEmpty()) {
            return "No roast history yet."
        }

        return list.joinToString("\n\n────────\n\n") {

            """
BATCH
${it.batchId}

STATUS
${it.batchStatus}

TIME
${formatTime(it.createdAtMillis)}
            """.trimIndent()
        }
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
}
