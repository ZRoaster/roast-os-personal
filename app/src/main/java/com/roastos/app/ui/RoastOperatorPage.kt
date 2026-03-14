package com.roastos.app.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.roastos.app.*
import kotlin.math.abs

object RoastOperatorPage {

    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST OPERATOR"))
        root.addView(UiKit.pageSubtitle(context, "Focus, insight, and control"))
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(UiKit.cardTitle(context, "ROAST STATUS"))
        statusCard.addView(statusBody)
        root.addView(statusCard)
        root.addView(UiKit.spacer(context))

        val actionFocusCard = UiKit.card(context)
        val actionFocusPanel = RoastActionFocusPanel(context)
        actionFocusCard.addView(UiKit.cardTitle(context, "ACTION FOCUS"))
        actionFocusCard.addView(actionFocusPanel)
        root.addView(actionFocusCard)
        root.addView(UiKit.spacer(context))

        val executiveCard = UiKit.card(context)
        val executivePanel = RoastExecutiveSummaryPanel(context)
        executiveCard.addView(UiKit.cardTitle(context, "EXECUTIVE SUMMARY"))
        executiveCard.addView(executivePanel)
        root.addView(executiveCard)
        root.addView(UiKit.spacer(context))

        val insightCard = UiKit.card(context)
        val insightPanel = RoastInsightPanel(context)
        insightCard.addView(UiKit.cardTitle(context, "INSIGHT"))
        insightCard.addView(insightPanel)
        root.addView(insightCard)
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
        val startBtn = UiKit.primaryButton(context, "START ROAST")
        val stopBtn = UiKit.secondaryButton(context, "STOP ROAST")
        val refreshBtn = UiKit.secondaryButton(context, "REFRESH")
        val openStudioBtn = UiKit.secondaryButton(context, "OPEN STUDIO")
        val openEnvironmentBtn = UiKit.secondaryButton(context, "OPEN ENVIRONMENT")
        val openRecentBtn = UiKit.secondaryButton(context, "OPEN RECENT ROASTS")
        val openLatestBtn = UiKit.secondaryButton(context, "OPEN LATEST HISTORY")

        controlCard.addView(UiKit.cardTitle(context, "QUICK CONTROL"))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(openStudioBtn)
        controlCard.addView(openEnvironmentBtn)
        controlCard.addView(openRecentBtn)
        controlCard.addView(openLatestBtn)
        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val snapshotCard = UiKit.card(context)
        val snapshotBody = UiKit.bodyText(context, "")
        val lastCompareTargetBody = UiKit.bodyText(context, "")
        val openLastDetailBtn = UiKit.secondaryButton(context, "OPEN LAST DETAIL")
        val openLastCompareBtn = UiKit.secondaryButton(context, "OPEN LAST COMPARE")
        val openRecentBtnTop = UiKit.secondaryButton(context, "OPEN RECENT ROASTS")

        snapshotCard.addView(UiKit.cardTitle(context, "LAST ROAST SNAPSHOT"))
        snapshotCard.addView(snapshotBody)
        snapshotCard.addView(UiKit.spacer(context))
        snapshotCard.addView(UiKit.cardTitle(context, "LAST COMPARE TARGET"))
        snapshotCard.addView(lastCompareTargetBody)
        snapshotCard.addView(UiKit.spacer(context))
        snapshotCard.addView(openLastDetailBtn)
        snapshotCard.addView(openLastCompareBtn)
        snapshotCard.addView(openRecentBtnTop)

        root.addView(snapshotCard)
        root.addView(UiKit.spacer(context))

        val actionableCard = UiKit.card(context)
        val actionableBody = UiKit.bodyText(context, "")

        actionableCard.addView(UiKit.cardTitle(context, "LAST REFERENCE"))
        actionableCard.addView(actionableBody)

        root.addView(actionableCard)
        root.addView(UiKit.spacer(context))

        val lastVsCurrentCard = UiKit.card(context)
        val lastVsCurrentBody = UiKit.bodyText(context, "")

        lastVsCurrentCard.addView(UiKit.cardTitle(context, "LAST VS CURRENT"))
        lastVsCurrentCard.addView(lastVsCurrentBody)

        root.addView(lastVsCurrentCard)
        root.addView(UiKit.spacer(context))

        val deviationCard = UiKit.card(context)
        val deviationBody = UiKit.bodyText(context, "")

        deviationCard.addView(UiKit.cardTitle(context, "REFERENCE DEVIATION ALERT"))
        deviationCard.addView(deviationBody)

        root.addView(deviationCard)

        fun renderLastSnapshot() {
            val latest = RoastHistoryEngine.latest()
            val historyCount = RoastHistoryEngine.all().size

            snapshotBody.text = if (latest == null) {
                """
No roast history yet.

Save your first roast to create a usable reference.
                """.trimIndent()
            } else {
                val fc = formatElapsed(
                    latest.actualFcSec ?: latest.predictedFcSec ?: 0,
                    allowDash = latest.actualFcSec == null && latest.predictedFcSec == null
                )
                val drop = formatElapsed(
                    latest.actualDropSec ?: latest.predictedDropSec ?: 0,
                    allowDash = latest.actualDropSec == null && latest.predictedDropSec == null
                )

                """
Batch
${latest.batchId}

Health
${latest.batchStatus} / ${latest.roastHealthHeadline}

Evaluation
${if (latest.evaluation != null) "Saved" else "Not saved"}

Environment
${latest.envTemp} ℃ / ${latest.envRh} %

FC / Drop
$fc / $drop

Created
${formatDateTime(latest.createdAtMillis)}
                """.trimIndent()
            }

            val allEntries = RoastHistoryEngine.all()
            lastCompareTargetBody.text = if (allEntries.size < 2) {
                """
Need at least 2 roast history entries.

The latest compare target will appear here automatically.
                """.trimIndent()
            } else {
                val latestEntry = allEntries[0]
                val previous = allEntries[1]

                """
A
${previous.batchId}

B
${latestEntry.batchId}
                """.trimIndent()
            }

            openLastDetailBtn.isEnabled = latest != null
            openLastCompareBtn.isEnabled = historyCount >= 2
        }

        fun renderLastActionableReference() {
            val latest = RoastHistoryEngine.latest()

            actionableBody.text = if (latest == null) {
                """
No actionable reference yet.

Save a roast history entry to unlock last-batch timing and health reference.
                """.trimIndent()
            } else {
                val turning = formatElapsed(
                    latest.actualTurningSec ?: latest.predictedTurningSec ?: 0,
                    allowDash = latest.actualTurningSec == null && latest.predictedTurningSec == null
                )
                val yellow = formatElapsed(
                    latest.actualYellowSec ?: latest.predictedYellowSec ?: 0,
                    allowDash = latest.actualYellowSec == null && latest.predictedYellowSec == null
                )
                val fc = formatElapsed(
                    latest.actualFcSec ?: latest.predictedFcSec ?: 0,
                    allowDash = latest.actualFcSec == null && latest.predictedFcSec == null
                )
                val drop = formatElapsed(
                    latest.actualDropSec ?: latest.predictedDropSec ?: 0,
                    allowDash = latest.actualDropSec == null && latest.predictedDropSec == null
                )

                """
Last Batch
${latest.batchId}

Turning / Yellow
$turning / $yellow

FC / Drop
$fc / $drop

Health / Evaluation
${latest.roastHealthHeadline} / ${if (latest.evaluation != null) "Saved" else "Not saved"}
                """.trimIndent()
            }
        }

        fun renderLastVsCurrent(
            session: RoastSessionState,
            validation: RoastValidationResult
        ) {
            val latest = RoastHistoryEngine.latest()
            val currentHealth = buildHealthHeadline(validation)

            lastVsCurrentBody.text = if (latest == null) {
                """
No previous roast reference yet.

Save a roast history entry to compare current operation against the last batch.
                """.trimIndent()
            } else {
                val currentEnv = AppState.lastPlannerInput
                val currentEnvTemp = currentEnv?.envTemp
                val currentEnvRh = currentEnv?.envRH

                val currentEnvText = if (currentEnvTemp == null || currentEnvRh == null) {
                    "- / -"
                } else {
                    "${oneDecimal(currentEnvTemp)} ℃ / ${oneDecimal(currentEnvRh)} %"
                }

                val lastEnvText = "${oneDecimal(latest.envTemp)} ℃ / ${oneDecimal(latest.envRh)} %"

                val lastTurning = formatElapsed(
                    latest.actualTurningSec ?: latest.predictedTurningSec ?: 0,
                    allowDash = latest.actualTurningSec == null && latest.predictedTurningSec == null
                )

                val lastYellow = formatElapsed(
                    latest.actualYellowSec ?: latest.predictedYellowSec ?: 0,
                    allowDash = latest.actualYellowSec == null && latest.predictedYellowSec == null
                )

                val lastFc = formatElapsed(
                    latest.actualFcSec ?: latest.predictedFcSec ?: 0,
                    allowDash = latest.actualFcSec == null && latest.predictedFcSec == null
                )

                val lastDrop = formatElapsed(
                    latest.actualDropSec ?: latest.predictedDropSec ?: 0,
                    allowDash = latest.actualDropSec == null && latest.predictedDropSec == null
                )

                """
Last Batch
${latest.batchId}

Environment
Current  $currentEnvText
Last     $lastEnvText

Timeline
Current Elapsed        ${formatElapsed(session.lastElapsedSec)}
Last Turning / Yellow  $lastTurning / $lastYellow
Last FC / Drop         $lastFc / $lastDrop

Health
Current  $currentHealth
Last     ${latest.roastHealthHeadline}
                """.trimIndent()
            }
        }

        fun renderDeviationAlert(
            session: RoastSessionState,
            validation: RoastValidationResult
        ) {
            val latest = RoastHistoryEngine.latest()

            deviationBody.text = if (latest == null) {
                """
No deviation reference yet.

Save a roast history entry to enable lightweight deviation alerts.
                """.trimIndent()
            } else {
                val alerts = mutableListOf<String>()
                val currentElapsed = session.lastElapsedSec

                val lastYellow = latest.actualYellowSec ?: latest.predictedYellowSec
                val lastFc = latest.actualFcSec ?: latest.predictedFcSec

                val currentHealthScore = riskScore(buildHealthHeadline(validation))
                val lastHealthScore = riskScore(latest.roastHealthHeadline)
                if (currentHealthScore > lastHealthScore && currentHealthScore > 0) {
                    alerts += """
Current health is worse than the last saved roast.
Watch late-stage stability before repeating the same finish.
                    """.trimIndent()
                }

                if (lastYellow != null && currentElapsed >= lastYellow + 20) {
                    alerts += """
Current elapsed is already beyond last yellow reference.
Review whether the roast is intentionally running slower than the previous batch.
                    """.trimIndent()
                }

                if (lastFc != null && currentElapsed >= lastFc - 15) {
                    alerts += """
Current elapsed is already close to last first crack reference.
Verify whether the current mid-late pace is aligned with intent.
                    """.trimIndent()
                }

                val currentEnv = AppState.lastPlannerInput
                val currentEnvTemp = currentEnv?.envTemp
                val currentEnvRh = currentEnv?.envRH
                val envShiftDetected =
                    currentEnvTemp != null &&
                        currentEnvRh != null &&
                        (abs(currentEnvTemp - latest.envTemp) >= 1.5 || abs(currentEnvRh - latest.envRh) >= 8.0)

                if (envShiftDetected) {
                    alerts += """
Current environment differs clearly from the last saved roast.
Do not copy previous phase expectations without adjustment.
                    """.trimIndent()
                }

                if (alerts.isEmpty()) {
                    """
No strong deviation alert under current rules.

Use LAST VS CURRENT as a soft reference and keep following the roast rhythm.
                    """.trimIndent()
                } else {
                    alerts.take(2).joinToString("\n\n")
                }
            }
        }

        fun render() {
            val snapshot = RoastSessionBus.tick()
            val session = snapshot.session

            statusBody.text =
                """
状态
${session.status}

核心读数
BT   ${String.format("%.1f", session.lastBeanTemp)} ℃
RoR  ${String.format("%.1f", session.lastRor)} ℃/min
时间  ${formatElapsed(session.lastElapsedSec)}

阶段 / 健康
${snapshot.companion.phaseLabel} / ${buildHealthHeadline(snapshot.validation)}
                """.trimIndent()

            renderLastSnapshot()
            renderLastActionableReference()
            renderLastVsCurrent(session, snapshot.validation)
            renderDeviationAlert(session, snapshot.validation)
            actionFocusPanel.update()
            executivePanel.update()
            insightPanel.update()
            advisorPanel.update()
            predictionPanel.update()
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

        openStudioBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        openEnvironmentBtn.setOnClickListener {
            EnvironmentInputPage.show(context, container)
        }

        openRecentBtn.setOnClickListener {
            RecentRoastListPage.show(
                context = context,
                container = container,
                onBack = {
                    show(context, container)
                }
            )
        }

        openRecentBtnTop.setOnClickListener {
            RecentRoastListPage.show(
                context = context,
                container = container,
                onBack = {
                    show(context, container)
                }
            )
        }

        openLatestBtn.setOnClickListener {
            HistoryDetailPage.show(
                context = context,
                container = container,
                entry = RoastHistoryEngine.latest(),
                onBack = {
                    show(context, container)
                }
            )
        }

        openLastDetailBtn.setOnClickListener {
            HistoryDetailPage.show(
                context = context,
                container = container,
                entry = RoastHistoryEngine.latest(),
                onBack = {
                    show(context, container)
                }
            )
        }

        openLastCompareBtn.setOnClickListener {
            val allEntries = RoastHistoryEngine.all()

            if (allEntries.size < 2) {
                Toast.makeText(
                    context,
                    "Need at least 2 roast history entries",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val latest = allEntries[0]
            val previous = allEntries[1]

            RoastComparePage.show(
                context = context,
                container = container,
                left = previous,
                right = latest,
                onBack = {
                    show(context, container)
                }
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

        running = false
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

    private fun riskScore(headline: String): Int {
        val text = headline.lowercase(java.util.Locale.getDefault())
        return when {
            "高风险" in headline -> 4
            "中风险" in headline -> 3
            "需留意" in headline -> 2
            "低风险" in headline -> 1
            "high" in text -> 4
            "medium" in text -> 3
            "watch" in text -> 2
            "low" in text -> 1
            else -> 0
        }
    }

    private fun formatElapsed(
        sec: Int,
        allowDash: Boolean = false
    ): String {
        if (allowDash && sec <= 0) return "-"
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatDateTime(ms: Long): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(ms))
    }

    private fun oneDecimal(value: Double): String {
        return String.format(java.util.Locale.getDefault(), "%.1f", value)
    }
}
