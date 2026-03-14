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
        root.addView(UiKit.pageSubtitle(context, "Connect, verify, observe, and act"))
        root.addView(UiKit.spacer(context))

        val connectionCard = UiKit.card(context)
        val connectionBody = UiKit.bodyText(context, "")
        connectionCard.addView(UiKit.cardTitle(context, "MACHINE CONNECTION"))
        connectionCard.addView(
            UiKit.helperText(
                context,
                "Confirm connection state first. Do not trust live operating guidance until the machine and telemetry state are clear."
            )
        )
        connectionCard.addView(UiKit.spacer(context))
        connectionCard.addView(UiKit.sectionLabel(context, "CONNECTION"))
        connectionCard.addView(connectionBody)
        root.addView(connectionCard)
        root.addView(UiKit.spacer(context))

        val readinessCard = UiKit.card(context)
        val readinessBody = UiKit.bodyText(context, "")
        readinessCard.addView(UiKit.cardTitle(context, "ROAST READINESS"))
        readinessCard.addView(
            UiKit.helperText(
                context,
                "Check whether environment, session, and latest machine state are ready enough to begin or continue operation."
            )
        )
        readinessCard.addView(UiKit.spacer(context))
        readinessCard.addView(UiKit.sectionLabel(context, "READINESS"))
        readinessCard.addView(readinessBody)
        root.addView(readinessCard)
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(UiKit.cardTitle(context, "CURRENT STATUS"))
        statusCard.addView(
            UiKit.helperText(
                context,
                "See where the roast is now, the core readings, and whether the current run looks healthy."
            )
        )
        statusCard.addView(UiKit.spacer(context))
        statusCard.addView(UiKit.sectionLabel(context, "STATUS"))
        statusCard.addView(statusBody)
        root.addView(statusCard)
        root.addView(UiKit.spacer(context))

        val judgmentCard = UiKit.card(context)
        val judgmentBody = UiKit.bodyText(context, "")
        judgmentCard.addView(UiKit.cardTitle(context, "CURRENT JUDGMENT"))
        judgmentCard.addView(
            UiKit.helperText(
                context,
                "Read one short observation first, then a compact system interpretation."
            )
        )
        judgmentCard.addView(UiKit.spacer(context))
        judgmentCard.addView(UiKit.sectionLabel(context, "OBSERVATION"))
        judgmentCard.addView(judgmentBody)
        root.addView(judgmentCard)
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        val actionBody = UiKit.bodyText(context, "")
        actionCard.addView(UiKit.cardTitle(context, "CURRENT ACTION"))
        actionCard.addView(
            UiKit.helperText(
                context,
                "This is the default action layer for heat, airflow, and immediate operating priority."
            )
        )
        actionCard.addView(UiKit.spacer(context))
        actionCard.addView(UiKit.sectionLabel(context, "ACTION"))
        actionCard.addView(actionBody)
        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val riskCard = UiKit.card(context)
        val riskBody = UiKit.bodyText(context, "")
        riskCard.addView(UiKit.cardTitle(context, "RISK / REFERENCE"))
        riskCard.addView(
            UiKit.helperText(
                context,
                "Check predicted crack timing, current risk, and whether the roast is drifting from the latest usable reference."
            )
        )
        riskCard.addView(UiKit.spacer(context))
        riskCard.addView(UiKit.sectionLabel(context, "RISK"))
        riskCard.addView(riskBody)
        root.addView(riskCard)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        val startBtn = UiKit.primaryButton(context, "START ROAST")
        val stopBtn = UiKit.secondaryButton(context, "STOP ROAST")
        val refreshBtn = UiKit.secondaryButton(context, "REFRESH")
        val openStudioBtn = UiKit.secondaryButton(context, "OPEN STUDIO")
        val openEnvironmentBtn = UiKit.secondaryButton(context, "OPEN ENVIRONMENT")
        val openRecentBtn = UiKit.secondaryButton(context, "OPEN RECENT ROASTS")
        val openLatestBtn = UiKit.secondaryButton(context, "OPEN LATEST HISTORY")
        val openLastCompareBtn = UiKit.secondaryButton(context, "OPEN LAST COMPARE")

        controlCard.addView(UiKit.cardTitle(context, "QUICK CONTROL"))
        controlCard.addView(
            UiKit.helperText(
                context,
                "Use these controls after connection and readiness are confirmed."
            )
        )
        controlCard.addView(UiKit.spacer(context))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(openStudioBtn)
        controlCard.addView(openEnvironmentBtn)
        controlCard.addView(openRecentBtn)
        controlCard.addView(openLatestBtn)
        controlCard.addView(openLastCompareBtn)
        root.addView(controlCard)

        fun renderConnection() {
            val machine = RoastStateModel.machine
            val session = RoastSessionBus.peek()?.session

            val sessionState = when {
                session == null -> "No session"
                session.status.isBlank() -> "Unknown"
                else -> session.status
            }

            val telemetryState = when {
                session == null -> "No live session snapshot"
                session.lastElapsedSec > 0 || session.lastBeanTemp > 0.0 -> "Live or simulated data present"
                else -> "No meaningful live telemetry yet"
            }

            val controlMode = "Assist / manual-safe"
            val connectionStatus = when {
                telemetryState == "Live or simulated data present" -> "Connected or session-fed"
                else -> "Not confirmed"
            }

            connectionBody.text = """
Connection
$connectionStatus

Control Mode
$controlMode

Telemetry
$telemetryState

Session
$sessionState

Machine Profile
HB M2SE

Synced Machine State
Power Delay ${formatDouble(machine.powerResponseDelay)} s
Air Delay ${formatDouble(machine.airflowResponseDelay)} s
RPM Delay ${formatDouble(machine.rpmResponseDelay)} s
            """.trimIndent()
        }

        fun renderReadiness(snapshot: RoastSessionBusSnapshot) {
            val plannerInput = AppState.lastPlannerInput
            val hasEnvironment = plannerInput?.envTemp != null && plannerInput.envRH != null
            val environmentText = if (hasEnvironment) {
                "${oneDecimal(plannerInput!!.envTemp)} ℃ / ${oneDecimal(plannerInput.envRH)} %"
            } else {
                "Missing"
            }

            val session = snapshot.session
            val hasTelemetry = session.lastBeanTemp > 0.0 || session.lastElapsedSec > 0
            val sessionText = if (session.status.isBlank()) "Unknown" else session.status

            val readiness = when {
                !hasEnvironment && !hasTelemetry -> "Not ready"
                !hasEnvironment -> "Partially ready"
                !hasTelemetry -> "Connection unclear"
                else -> "Ready for guided operation"
            }

            val note = when (readiness) {
                "Ready for guided operation" -> "Environment and session data are present."
                "Connection unclear" -> "Environment exists, but live telemetry is still weak."
                "Partially ready" -> "Session exists, but environment input is missing."
                else -> "Set environment and verify telemetry before relying on live guidance."
            }

            readinessBody.text = """
Readiness
$readiness

Environment
$environmentText

Session
$sessionText

Telemetry
${if (hasTelemetry) "Present" else "Weak / absent"}

Note
$note
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
            """.trimIndent()
        }

        fun renderRiskAndReference(
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

            val parts = mutableListOf<String>()

            parts += """
当前健康
$currentHealth

预测风险
$predictionRisk

预计一爆
$fcText

预测说明
$predictionReason
            """.trimIndent()

            if (latest == null) {
                parts += """
参考状态
No roast history yet.

Save the first usable roast to unlock comparison-based guidance.
                """.trimIndent()
            } else {
                val alerts = mutableListOf<String>()
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

                val alertText = if (alerts.isEmpty()) {
                    "No strong deviation under current rules."
                } else {
                    alerts.take(2).joinToString("\n")
                }

                val currentEnvText = if (currentEnvTemp == null || currentEnvRh == null) {
                    "- / -"
                } else {
                    "${oneDecimal(currentEnvTemp)} ℃ / ${oneDecimal(currentEnvRh)} %"
                }

                parts += """
参考锅
${latest.batchId}

环境
Current  $currentEnvText
Last     ${oneDecimal(latest.envTemp)} ℃ / ${oneDecimal(latest.envRh)} %

参考预警
$alertText
                """.trimIndent()
            }

            riskBody.text = parts.joinToString("\n\n")
        }

        fun render() {
            val snapshot = RoastSessionBus.tick()
            val session = snapshot.session

            renderConnection()
            renderReadiness(snapshot)
            renderStatus(snapshot)
            renderJudgment(snapshot)
            renderAction(snapshot)
            renderRiskAndReference(session, snapshot.validation)

            val allEntries = RoastHistoryEngine.all()
            openLastCompareBtn.isEnabled = allEntries.size >= 2
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

        openLatestBtn.setOnClickListener {
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

    private fun formatDouble(value: Double): String {
        return String.format(java.util.Locale.getDefault(), "%.2f", value)
    }

    private fun formatDateTime(ms: Long): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(ms))
    }

    private fun oneDecimal(value: Double): String {
        return String.format(java.util.Locale.getDefault(), "%.1f", value)
    }
}
