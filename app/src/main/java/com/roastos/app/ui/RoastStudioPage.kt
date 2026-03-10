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
        val subtitle = UiKit.pageSubtitle(context, "Live roast session")

        root.addView(title)
        root.addView(subtitle)
        root.addView(UiKit.spacer(context))

        val curveCard = UiKit.card(context)
        val curveTitle = UiKit.cardTitle(context, "CURVE")
        val curvePanel = RoastCurvePanel(context)

        curveCard.addView(curveTitle)
        curveCard.addView(curvePanel)

        root.addView(curveCard)
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
        val stateTitle = UiKit.cardTitle(context, "SESSION")
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

            val session = RoastSessionEngine.currentState()

            RoastPhaseDetectionEngine.update(session)
            RoastLogEngine.update(session)

            curvePanel.update()

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

            phaseBody.text =
                RoastPhaseDetectionEngine.summary()

            logBody.text =
                RoastLogEngine.buildLogText(session)

            historyBody.text =
                buildHistoryPanelText()
        }

        startBtn.setOnClickListener {

            RoastSessionEngine.reset()
            RoastPhaseDetectionEngine.reset()
            RoastLogEngine.reset()

            MachineBridge.start()

            running = true
        }

        stopBtn.setOnClickListener {

            val session = RoastSessionEngine.currentState()

            RoastHistoryEngine.saveCurrentRoastLog(
                session = session,
                machineName = "HB M2SE"
            )

            MachineBridge.stop()

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

    private fun buildHistoryPanelText(): String {

        val all = RoastHistoryEngine.all()

        if (all.isEmpty()) {
            return """
Count
0

Latest Batch
-

Recent Roasts
-
            """.trimIndent()
        }

        val latest = all.first()

        val recentEntries = all.take(5).mapIndexed { index, entry ->
            buildString {
                append(index + 1)
                append(". ")
                append(entry.title)
                append("\n")

                append("Batch ID\n")
                append(entry.batchId)
                append("\n\n")

                append("Status\n")
                append(entry.batchStatus)
                append("\n\n")

                append("Total Time\n")
                append(extractTotalTime(entry.reportText))
                append("\n\n")

                append("Drop Temp\n")
                append(extractField(entry.reportText, "Drop Temp"))
                append("\n\n")

                append("First Crack\n")
                append(extractField(entry.reportText, "First Crack"))
                append("\n\n")

                append("Development Ratio\n")
                append(extractField(entry.reportText, "Development Ratio"))
            }
        }.joinToString("\n\n────────\n\n")

        return """
Count
${all.size}

Latest Batch
${latest.batchId}

Latest Status
${latest.batchStatus}

Recent Roasts
$recentEntries
        """.trimIndent()
    }

    private fun extractTotalTime(reportText: String): String {
        return extractField(reportText, "Total Time")
    }

    private fun extractField(reportText: String, field: String): String {
        val lines = reportText.lines()

        for (i in lines.indices) {
            if (lines[i].trim() == field) {
                val nextIndex = i + 1
                if (nextIndex in lines.indices) {
                    val value = lines[nextIndex].trim()
                    if (value.isNotEmpty()) {
                        return value
                    }
                }
            }
        }

        return "-"
    }

    private fun formatElapsed(sec: Int): String {
        val minutes = sec / 60
        val seconds = sec % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
