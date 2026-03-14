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
        root.addView(UiKit.pageSubtitle(context, "Observe, decide, and act"))
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(UiKit.cardTitle(context, "CURRENT STATUS"))
        statusCard.addView(UiKit.helperText(context, "See where the roast is, whether it is stable, and what matters now."))
        statusCard.addView(UiKit.spacer(context))
        statusCard.addView(UiKit.sectionLabel(context, "STATUS"))
        statusCard.addView(statusBody)
        root.addView(statusCard)
        root.addView(UiKit.spacer(context))

        val judgmentCard = UiKit.card(context)
        val judgmentBody = UiKit.bodyText(context, "")
        judgmentCard.addView(UiKit.cardTitle(context, "CURRENT JUDGMENT"))
        judgmentCard.addView(UiKit.helperText(context, "One short observation first, then a compact system reading."))
        judgmentCard.addView(UiKit.spacer(context))
        judgmentCard.addView(UiKit.sectionLabel(context, "OBSERVATION"))
        judgmentCard.addView(judgmentBody)
        root.addView(judgmentCard)
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        val actionBody = UiKit.bodyText(context, "")
        actionCard.addView(UiKit.cardTitle(context, "CURRENT ACTION"))
        actionCard.addView(UiKit.helperText(context, "This is the default action layer for heat, airflow, and immediate operating focus."))
        actionCard.addView(UiKit.spacer(context))
        actionCard.addView(UiKit.sectionLabel(context, "ACTION"))
        actionCard.addView(actionBody)
        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val riskCard = UiKit.card(context)
        val riskBody = UiKit.bodyText(context, "")
        riskCard.addView(UiKit.cardTitle(context, "RISK / PREDICTION"))
        riskCard.addView(UiKit.helperText(context, "Check risk, crack timing, and whether the current roast is drifting from usable reference."))
        riskCard.addView(UiKit.spacer(context))
        riskCard.addView(UiKit.sectionLabel(context, "RISK"))
        riskCard.addView(riskBody)
        root.addView(riskCard)
        root.addView(UiKit.spacer(context))

        val compareCard = UiKit.card(context)
        val compareBody = UiKit.bodyText(context, "")
        compareCard.addView(UiKit.cardTitle(context, "LAST VS CURRENT"))
        compareCard.addView(UiKit.helperText(context, "Use the latest saved roast as a soft reference for pace, environment, and health."))
        compareCard.addView(UiKit.spacer(context))
        compareCard.addView(UiKit.sectionLabel(context, "COMPARISON"))
        compareCard.addView(compareBody)
        root.addView(compareCard)
        root.addView(UiKit.spacer(context))

        val historyCard = UiKit.card(context)
        val historyBody = UiKit.bodyText(context, "")
        val openLastDetailBtn = UiKit.secondaryButton(context, "OPEN LAST DETAIL")
        val openLastCompareBtn = UiKit.secondaryButton(context, "OPEN LAST COMPARE")
        val openRecentBtnTop = UiKit.secondaryButton(context, "OPEN RECENT ROASTS")

        historyCard.addView(UiKit.cardTitle(context, "HISTORY REFERENCE"))
        historyCard.addView(UiKit.helperText(context, "Review the latest saved batch and jump to comparison when you need deeper context."))
        historyCard.addView(UiKit.spacer(context))
        historyCard.addView(UiKit.sectionLabel(context, "REFERENCE"))
        historyCard.addView(historyBody)
        historyCard.addView(UiKit.spacer(context))
        historyCard.addView(openLastDetailBtn)
        historyCard.addView(openLastCompareBtn)
        historyCard.addView(openRecentBtnTop)
        root.addView(historyCard)
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
        controlCard.addView(UiKit.helperText(context, "Use these controls after you have checked the current status, judgment, and action."))
        controlCard.addView(UiKit.spacer(context))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(openStudioBtn)
        controlCard.addView(openEnvironmentBtn)
        controlCard.addView(openRecentBtn)
        controlCard.addView(openLatestBtn)
        root.addView(controlCard)

        fun renderHistoryReference() {
            val latest = RoastHistoryEngine.latest()
            val allEntries = RoastHistoryEngine.all()

            historyBody.text = if (latest == null) {
                """
No roast history yet.

Save your first roast to create a usable reference.
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

                val compareTargetText = if (allEntries.size >= 2) {
                    val previous = allEntries[1]
                    "A ${previous.batchId} / B ${latest.batchId}"
                } else {
                    "Need at least 2 saved batches."
                }

                """
Last Batch
${latest.batchId}

Health
${latest.batchStatus} / ${latest.roastHealthHeadline}

Evaluation
${if (latest.evaluation != null) "Saved" else "Not saved"}

Turning / Yellow
$turning / $yellow

FC / Drop
$fc / $drop

Last Compare
$compareTargetText

Created
${formatDateTime(latest.createdAtMillis)}
                """.trimIndent()
            }

            openLastDetailBtn.isEnabled = latest != null
            openLastCompareBtn.isEnabled = allEntries.size >= 2
        }

        fun renderLastVsCurrent(
            session: RoastSessionState,
            validation: RoastValidationResult
        ) {
            val latest = RoastHistoryEngine.latest()
            val currentHealth = buildHealthHeadline(validation)

            compareBody.text = if (latest == null) {
                """
No previous roast reference yet.

Save a roast history entry to compare current operation against the latest batch.
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
Current Elapsed
${formatElapsed(session.lastElapsedSec)}

Environment
Current  $currentEnvText
Last     $lastEnvText

Last Reference
Turning / Yellow  $lastTurning / $lastYellow
FC / Drop         $lastFc / $lastDrop

Health
Current  $currentHealth
Last     ${latest.roastHealthHeadline}
                """.trimIndent()
            }
        }

        fun renderRiskAndPrediction(
            session: RoastSessionState,
            validation: RoastValidationResult
        ) {
            val snapshot = RoastSessionBus.peek()
            val prediction = snapshot?.let { RoastRorPredictionEngine.evaluate(it) }
            val latest = RoastHistoryEngine.latest()

            val currentHealth = buildHealthHeadline(validation)
            val fcText = prediction?.estimatedFirstCrackWindowSec?.let { formatElapsed(it) } ?: "-"
            val predictionRisk = prediction?.predictedRisk ?: "-"
            val predictionReason = prediction?.reason?.ifBlank { "-" } ?: "-"

            val alerts = mutableListOf<String>()

            if (latest != null) {
                val currentElapsed = session.lastElapsedSec
                val lastYellow = latest.actualYellowSec ?: latest.predictedYellowSec
                val lastFc = latest.actualFcSec ?: latest.predictedFcSec

                val currentHealthScore = riskScore(currentHealth)
                val lastHealthScore = riskScore(latest.roastHealthHeadline)
                if (currentHealthScore > lastHealthScore && currentHealthScore > 0) {
                    alerts += "Current health is weaker than the latest saved roast."
                }

                if (lastYellow != null && currentElapsed >= lastYellow + 20) {
                    alerts += "Current pace is already slower than the last yellow reference."
                }

                if (lastFc != null && currentElapsed >= lastFc - 15) {
                    alerts += "Current pace is already close to the last first crack reference."
                }

                val currentEnv = AppState.lastPlannerInput
                val currentEnvTemp = currentEnv?.envTemp
                val currentEnvRh = currentEnv?.envRH
                val envShiftDetected =
                    currentEnvTemp != null &&
                        currentEnvRh != null &&
                        (abs(currentEnvTemp - latest.envTemp) >= 1.5 || abs(currentEnvRh - latest.envRh) >= 8.0)

                if (envShiftDetected) {
                    alerts += "Environment differs clearly from the latest saved roast."
                }
            }

            val alertText = if (alerts.isEmpty()) {
                "No strong reference deviation under current rules."
            } else {
                alerts.take(2).joinToString("\n")
            }

            riskBody.text = """
Current Health
$currentHealth

Predicted Risk
$predictionRisk

Estimated First Crack
$fcText

Prediction Note
$predictionReason

Reference Alert
$alertText
            """.trimIndent()
        }

        fun renderJudgment(snapshot: RoastSessionBusSnapshot) {
            val headline = RoastInsightBridge.observationHeadlineForSnapshot(snapshot)
            val summary = RoastInsightBridge.quietSummaryForSnapshot(snapshot)

            judgmentBody.text = """
$headline

系统理解
$summary
            """.trimIndent()
        }

        fun renderAction(snapshot: RoastSessionBusSnapshot) {
            val output = RoastControlAdvisorEngine.evaluate(snapshot)

            actionBody.text = """
阶段 / 优先级
${output.stage} / ${output.priority}

火力
${output.finalHeatAdvice}

风门
${output.finalAirflowAdvice}

风味方向
${output.flavorDirection}

风险 / 置信度
${output.riskLevel} / ${output.confidence}

参考状态
${output.referenceContextLevel}
            """.trimIndent()
        }

        fun renderStatus(snapshot: RoastSessionBusSnapshot) {
            val session = snapshot.session

            statusBody.text = """
状态
${session.status}

核心读数
BT   ${String.format("%.1f", session.lastBeanTemp)} ℃
RoR  ${String.format("%.1f", session.lastRor)} ℃/min
时间  ${formatElapsed(session.lastElapsedSec)}

阶段 / 健康
${snapshot.companion.phaseLabel} / ${buildHealthHeadline(snapshot.validation)}
            """.trimIndent()
        }

        fun render() {
            val snapshot = RoastSessionBus.tick()
            val session = snapshot.session

            renderStatus(snapshot)
            renderJudgment(snapshot)
            renderAction(snapshot)
            renderRiskAndPrediction(session, snapshot.validation)
            renderLastVsCurrent(session, snapshot.validation)
            renderHistoryReference()
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
                onBack = { show(context, container) }
            )
        }

        openRecentBtnTop.setOnClickListener {
            RecentRoastListPage.show(
                context = context,
                container = container,
                onBack = { show(context, container) }
            )
        }

        openLatestBtn.setOnClickListener {
            HistoryDetailPage.show(
                context = context,
                container = container,
                entry = RoastHistoryEngine.latest(),
                onBack = { show(context, container) }
            )
        }

        openLastDetailBtn.setOnClickListener {
            HistoryDetailPage.show(
                context = context,
                container = container,
                entry = RoastHistoryEngine.latest(),
                onBack = { show(context, container) }
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
                onBack = { show(context, container) }
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
