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

        val openRecentBtn =
            UiKit.secondaryButton(context, "OPEN RECENT ROASTS")

        val openLatestBtn =
            UiKit.secondaryButton(context, "OPEN LATEST HISTORY")

        val openKnowledgeBtn =
            UiKit.secondaryButton(context, "OPEN KNOWLEDGE")

        controlCard.addView(UiKit.cardTitle(context, "CONTROL"))

        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(openRecentBtn)
        controlCard.addView(openLatestBtn)
        controlCard.addView(openKnowledgeBtn)

        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val knowledgeCard = UiKit.card(context)
        val knowledgePanel = RoastKnowledgePanel(context)

        knowledgeCard.addView(
            UiKit.cardTitle(context, "KNOWLEDGE")
        )

        knowledgeCard.addView(knowledgePanel)

        root.addView(knowledgeCard)
        root.addView(UiKit.spacer(context))

        fun render() {

            val snapshot = RoastSessionBus.tick()
            val session = snapshot.session

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

            knowledgePanel.update()
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

    private fun formatElapsed(
        sec: Int
    ): String {

        val m = sec / 60
        val s = sec % 60

        return "%d:%02d".format(m, s)
    }
}
