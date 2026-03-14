package com.roastos.app.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.roastos.app.AppState
import com.roastos.app.RoastControlAdvisorEngine
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastInsightBridge
import com.roastos.app.RoastRorPredictionEngine
import com.roastos.app.RoastSessionBus
import com.roastos.app.RoastSessionBusSnapshot
import com.roastos.app.RoastSessionState
import com.roastos.app.RoastValidationResult
import com.roastos.app.UiKit
import kotlin.math.abs

object RoastOperatorPage {

    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private enum class OperatorUiState {
        DISCONNECTED,
        NOT_READY,
        ACTIVE
    }

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "OPERATE"))
        root.addView(UiKit.pageSubtitle(context, "Connect, judge, and act"))
        root.addView(UiKit.spacer(context))
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.OPERATE
            )
        )
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(UiKit.cardTitle(context, "OPERATOR STATUS"))
        statusCard.addView(
            UiKit.helperText(
                context,
                "See connection, readiness, and current roast state first."
            )
        )
        statusCard.addView(UiKit.spacer(context))
        statusCard.addView(UiKit.sectionLabel(context, "STATUS"))
        statusCard.addView(statusBody)
        root.addView(statusCard)
        root.addView(UiKit.spacer(context))

        val decisionCard = UiKit.card(context)
        val decisionBody = UiKit.bodyText(context, "")
        decisionCard.addView(UiKit.cardTitle(context, "DECISION"))
        decisionCard.addView(
            UiKit.helperText(
                context,
                "Read one short observation, then the immediate action and current risk."
            )
        )
        decisionCard.addView(UiKit.spacer(context))
        decisionCard.addView(UiKit.sectionLabel(context, "DECISION"))
        decisionCard.addView(decisionBody)
        root.addView(decisionCard)
        root.addView(UiKit.spacer(context))

        val referenceCard = UiKit.card(context)
        val referenceBody = UiKit.bodyText(context, "")
        referenceCard.addView(UiKit.cardTitle(context, "REFERENCE CHECK"))
        referenceCard.addView(
            UiKit.helperText(
                context,
                "Use the latest saved roast as a soft reference for drift, not as a rigid command."
            )
        )
        referenceCard.addView(UiKit.spacer(context))
        referenceCard.addView(UiKit.sectionLabel(context, "REFERENCE"))
        referenceCard.addView(referenceBody)
        root.addView(referenceCard)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        val startBtn = UiKit.primaryButton(context, "START ROAST")
        val stopBtn = UiKit.secondaryButton(context, "STOP ROAST")
        val refreshBtn = UiKit.secondaryButton(context, "REFRESH")
        val openEnvironmentBtn = UiKit.secondaryButton(context, "OPEN ENVIRONMENT")
        val openReviewBtn = UiKit.secondaryButton(context, "OPEN REVIEW")
        val openLatestBtn = UiKit.secondaryButton(context, "OPEN LAST DETAIL")
        val openLastCompareBtn = UiKit.secondaryButton(context, "OPEN LAST COMPARE")
        val backShellBtn = UiKit.secondaryButton(context, "BACK TO HOME")

        controlCard.addView(UiKit.cardTitle(context, "QUICK CONTROL"))
        controlCard.addView(
            UiKit.helperText(
                context,
                "Controls stay available, but always check status and decision first."
            )
        )
        controlCard.addView(UiKit.spacer(context))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(openEnvironmentBtn)
        controlCard.addView(openReviewBtn)
        controlCard.addView(openLatestBtn)
        controlCard.addView(openLastCompareBtn)
        controlCard.addView(backShellBtn)
        root.addView(controlCard)

        fun resolveUiState(snapshot: RoastSessionBusSnapshot): OperatorUiState {
            val session = snapshot.session
            val plannerInput = AppState.lastPlannerInput

            val hasEnvironment = plannerInput?.envTemp != null && plannerInput.envRH != null
            val hasTelemetry = session.lastBeanTemp > 0.0 || session.lastElapsedSec > 0
            val isActivelyRoasting = running ||
                session.lastElapsedSec > 0 ||
                session.status.toString().contains("roast", ignoreCase = true) ||
                session.status.toString().contains("running", ignoreCase = true)

            return when {
                !hasTelemetry -> OperatorUiState.DISCONNECTED
                !hasEnvironment || !isActivelyRoasting -> OperatorUiState.NOT_READY
                else -> OperatorUiState.ACTIVE
            }
        }

        fun renderDisconnected(snapshot: RoastSessionBusSnapshot) {
            val session = snapshot.session

            statusBody.text = """
当前状态
未连接 / 未确认

Session
${session.status}

Telemetry
${if (session.lastBeanTemp > 0.0 || session.lastElapsedSec > 0) "弱数据存在" else "未检测到有效实时数据"}

下一步
先确认机器连接或数据来源，再进入烘焙操作。
            """.trimIndent()

            decisionBody.text = """
当前判断
现在不适合读取实时烘焙建议。

当前动作
1. 检查连接
2. 补环境
3. 回到本页重新确认状态

当前风险
在连接未确认时，实时判断不应作为可靠依据。
            """.trimIndent()

            referenceBody.text = """
参考状态
${buildLatestReferenceStrip()}

说明
现在更适合进入准备和复盘，不适合进入实时决策。
            """.trimIndent()

            startBtn.isEnabled = false
            stopBtn.isEnabled = false
        }

        fun renderNotReady(snapshot: RoastSessionBusSnapshot) {
            val session = snapshot.session
            val plannerInput = AppState.lastPlannerInput
            val hasEnvironment = plannerInput?.envTemp != null && plannerInput.envRH != null
            val envText = if (hasEnvironment) {
                "${oneDecimal(plannerInput!!.envTemp)} ℃ / ${oneDecimal(plannerInput.envRH)} %"
            } else {
                "缺失"
            }

            val readinessNote = when {
                !hasEnvironment -> "缺少环境输入。"
                session.lastElapsedSec <= 0 -> "尚未进入有效烘焙进程。"
                else -> "当前数据不足以进入完整主操作流。"
            }

            statusBody.text = """
当前状态
已连接但未就绪

Session
${session.status}

Environment
$envText

Telemetry
${if (session.lastBeanTemp > 0.0 || session.lastElapsedSec > 0) "已存在" else "偏弱"}

下一步
$readinessNote
            """.trimIndent()

            decisionBody.text = """
当前观察
${RoastInsightBridge.observationHeadlineForSnapshot(snapshot)}

当前判断
这条观察现在只能作为弱提示。

当前动作
先补全准备条件，再进入完整操作模式。
            """.trimIndent()

            referenceBody.text = """
参考状态
${buildLatestReferenceStrip()}

建议
先完成准备，再开始读取更强的动作建议。
            """.trimIndent()

            startBtn.isEnabled = hasEnvironment
            stopBtn.isEnabled = false
        }

        fun renderActive(snapshot: RoastSessionBusSnapshot) {
            val session = snapshot.session
            val advisor = RoastControlAdvisorEngine.evaluate(snapshot)
            val prediction = RoastRorPredictionEngine.evaluate(snapshot)

            statusBody.text = """
连接 / 就绪
已进入操作状态

状态
${session.status}

核心读数
BT   ${String.format("%.1f", session.lastBeanTemp)} ℃
RoR  ${String.format("%.1f", session.lastRor)} ℃/min
时间  ${formatElapsed(session.lastElapsedSec)}

阶段 / 健康
${snapshot.companion.phaseLabel} / ${buildHealthHeadline(snapshot.validation)}
            """.trimIndent()

            val fcText = prediction.estimatedFirstCrackWindowSec?.let { formatElapsed(it) } ?: "-"
            val predictionRisk = prediction.predictedRisk
            val headline = RoastInsightBridge.observationHeadlineForSnapshot(snapshot)

            decisionBody.text = """
当前观察
$headline

当前动作
火力：${advisor.finalHeatAdvice}
风门：${advisor.finalAirflowAdvice}

优先级
${advisor.stage} / ${advisor.priority}

当前风险
${advisor.riskLevel} / ${advisor.confidence}

预计一爆
$fcText

风味方向
${advisor.flavorDirection}

预测风险
$predictionRisk
            """.trimIndent()

            referenceBody.text = buildActiveReferenceText(
                session = session,
                validation = snapshot.validation
            )

            startBtn.isEnabled = false
            stopBtn.isEnabled = true
        }

        fun render() {
            val snapshot = RoastSessionBus.tick()
            val uiState = resolveUiState(snapshot)

            when (uiState) {
                OperatorUiState.DISCONNECTED -> renderDisconnected(snapshot)
                OperatorUiState.NOT_READY -> renderNotReady(snapshot)
                OperatorUiState.ACTIVE -> renderActive(snapshot)
            }

            val allEntries = RoastHistoryEngine.all()
            openLatestBtn.isEnabled = RoastHistoryEngine.latest() != null
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

        openEnvironmentBtn.setOnClickListener {
            EnvironmentInputPage.show(context, container)
        }

        openReviewBtn.setOnClickListener {
            ReviewHubPage.show(
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

        backShellBtn.setOnClickListener {
            MainShellPage.show(context, container)
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

    private fun buildLatestReferenceStrip(): String {
        val latest = RoastHistoryEngine.latest() ?: return """
No roast history yet.

Save the first usable roast to unlock comparison-based reference.
        """.trimIndent()

        return """
最近参考锅
${latest.batchId}

健康
${latest.batchStatus} / ${latest.roastHealthHeadline}

评测
${if (latest.evaluation != null) "Saved" else "Not saved"}
        """.trimIndent()
    }

    private fun buildActiveReferenceText(
        session: RoastSessionState,
        validation: RoastValidationResult
    ): String {
        val latest = RoastHistoryEngine.latest()
        val currentHealth = buildHealthHeadline(validation)

        if (latest == null) {
            return """
参考状态
No roast history yet.

Save the first usable roast to unlock reference-based drift checks.
            """.trimIndent()
        }

        val alerts = mutableListOf<String>()
        val currentElapsed = session.lastElapsedSec

        val lastYellow = latest.actualYellowSec ?: latest.predictedYellowSec
        val lastFc = latest.actualFcSec ?: latest.predictedFcSec

        val currentHealthScore = riskScore(currentHealth)
        val lastHealthScore = riskScore(latest.roastHealthHeadline)
        if (currentHealthScore > lastHealthScore && currentHealthScore > 0) {
            alerts += "当前健康状态弱于最近参考锅。"
        }

        if (lastYellow != null && currentElapsed >= lastYellow + 20) {
            alerts += "当前节奏已慢于最近 yellow 参考。"
        }

        if (lastFc != null && currentElapsed >= lastFc - 15) {
            alerts += "当前节奏已接近最近一爆参考。"
        }

        val currentEnv = AppState.lastPlannerInput
        val currentEnvTemp = currentEnv?.envTemp
        val currentEnvRh = currentEnv?.envRH
        val envShiftDetected =
            currentEnvTemp != null &&
                currentEnvRh != null &&
                (abs(currentEnvTemp - latest.envTemp) >= 1.5 || abs(currentEnvRh - latest.envRh) >= 8.0)

        if (envShiftDetected) {
            alerts += "当前环境与最近参考锅差异明显。"
        }

        val currentEnvText = if (currentEnvTemp == null || currentEnvRh == null) {
            "- / -"
        } else {
            "${oneDecimal(currentEnvTemp)} ℃ / ${oneDecimal(currentEnvRh)} %"
        }

        return """
参考锅
${latest.batchId}

环境
Current  $currentEnvText
Last     ${oneDecimal(latest.envTemp)} ℃ / ${oneDecimal(latest.envRh)} %

参考预警
${if (alerts.isEmpty()) "无明显参考偏差。" else alerts.take(2).joinToString("\n")}
        """.trimIndent()
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

    private fun oneDecimal(value: Double): String {
        return String.format(java.util.Locale.getDefault(), "%.1f", value)
    }
}
