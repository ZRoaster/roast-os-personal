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
        root.addView(UiKit.spacerS(context))
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.OPERATE
            )
        )
        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        val summaryBody = UiKit.bodyText(context, "")
        summaryCard.addView(UiKit.cardTitle(context, "CURRENT STATE"))
        summaryCard.addView(UiKit.spacerS(context))
        summaryCard.addView(summaryBody)
        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val decisionCard = UiKit.card(context)
        val decisionBody = UiKit.bodyText(context, "")
        decisionCard.addView(UiKit.cardTitle(context, "CURRENT DECISION"))
        decisionCard.addView(UiKit.spacerS(context))
        decisionCard.addView(decisionBody)
        root.addView(decisionCard)
        root.addView(UiKit.spacer(context))

        val referenceCard = UiKit.card(context)
        val referenceBody = UiKit.bodyText(context, "")
        referenceCard.addView(UiKit.cardTitle(context, "REFERENCE"))
        referenceCard.addView(UiKit.spacerS(context))
        referenceCard.addView(referenceBody)
        root.addView(referenceCard)
        root.addView(UiKit.spacer(context))

        val primaryControlCard = UiKit.card(context)
        val startBtn = UiKit.primaryButton(context, "Start Roast")
        val stopBtn = UiKit.secondaryButton(context, "Stop Roast")
        val refreshBtn = UiKit.secondaryButton(context, "Refresh")

        primaryControlCard.addView(UiKit.cardTitle(context, "PRIMARY CONTROL"))
        primaryControlCard.addView(UiKit.helperText(context, "Live roast session controls."))
        primaryControlCard.addView(UiKit.spacerM(context))
        primaryControlCard.addView(startBtn)
        primaryControlCard.addView(UiKit.spacerS(context))
        primaryControlCard.addView(stopBtn)
        primaryControlCard.addView(UiKit.spacerS(context))
        primaryControlCard.addView(refreshBtn)
        root.addView(primaryControlCard)
        root.addView(UiKit.spacer(context))

        val accessCard = UiKit.card(context)
        val openEnvironmentBtn = UiKit.secondaryButton(context, "Environment")
        val openReviewBtn = UiKit.secondaryButton(context, "Review")
        val openLatestBtn = UiKit.secondaryButton(context, "Last Detail")
        val openLastCompareBtn = UiKit.secondaryButton(context, "Last Compare")
        val backShellBtn = UiKit.secondaryButton(context, "Home")

        accessCard.addView(UiKit.cardTitle(context, "MORE ACCESS"))
        accessCard.addView(UiKit.helperText(context, "Preparation, review, history, and home."))
        accessCard.addView(UiKit.spacerM(context))
        accessCard.addView(openEnvironmentBtn)
        accessCard.addView(UiKit.spacerS(context))
        accessCard.addView(openReviewBtn)
        accessCard.addView(UiKit.spacerS(context))
        accessCard.addView(openLatestBtn)
        accessCard.addView(UiKit.spacerS(context))
        accessCard.addView(openLastCompareBtn)
        accessCard.addView(UiKit.spacerS(context))
        accessCard.addView(backShellBtn)
        root.addView(accessCard)

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

            summaryBody.text = """
状态
未连接 / 未确认

Session / Telemetry
${session.status} / ${if (session.lastBeanTemp > 0.0 || session.lastElapsedSec > 0) "弱数据存在" else "未检测到有效实时数据"}

下一步
确认机器连接或数据来源
            """.trimIndent()

            decisionBody.text = """
当前判断
现在不适合读取实时烘焙建议

当前动作
检查连接 · 补环境 · 重新确认状态

当前风险
实时判断暂不可靠
            """.trimIndent()

            referenceBody.text = """
参考状态
${buildLatestReferenceStrip()}

当前建议
先做准备，不进入实时决策
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
                !hasEnvironment -> "缺少环境输入"
                session.lastElapsedSec <= 0 -> "尚未进入有效烘焙进程"
                else -> "当前数据不足以进入完整主操作流"
            }

            summaryBody.text = """
状态
已连接但未就绪

Session / Environment
${session.status} / $envText

Telemetry
${if (session.lastBeanTemp > 0.0 || session.lastElapsedSec > 0) "已存在" else "偏弱"}

下一步
$readinessNote
            """.trimIndent()

            decisionBody.text = """
当前观察
${RoastInsightBridge.observationHeadlineForSnapshot(snapshot)}

当前判断
只能作为弱提示

当前动作
先补全准备条件，再进入完整操作模式
            """.trimIndent()

            referenceBody.text = """
参考状态
${buildLatestReferenceStrip()}

当前建议
先完成准备，再读取更强建议
            """.trimIndent()

            startBtn.isEnabled = hasEnvironment
            stopBtn.isEnabled = false
        }

        fun renderActive(snapshot: RoastSessionBusSnapshot) {
            val session = snapshot.session
            val advisor = RoastControlAdvisorEngine.evaluate(snapshot)
            val prediction = RoastRorPredictionEngine.evaluate(snapshot)

            summaryBody.text = """
状态
已进入操作状态

Session
${session.status}

BT / RoR / 时间
${String.format("%.1f", session.lastBeanTemp)} ℃ / ${String.format("%.1f", session.lastRor)} ℃/min / ${formatElapsed(session.lastElapsedSec)}

阶段 / 健康
${snapshot.companion.phaseLabel} / ${buildHealthHeadline(snapshot.validation)}
            """.trimIndent()

            val fcText = prediction.estimatedFirstCrackWindowSec?.let { formatElapsed(it) } ?: "-"
            val headline = RoastInsightBridge.observationHeadlineForSnapshot(snapshot)

            decisionBody.text = """
当前观察
$headline

火力 / 风门
${advisor.finalHeatAdvice} / ${advisor.finalAirflowAdvice}

优先级
${advisor.stage} / ${advisor.priority}

风险 / 置信度
${advisor.riskLevel} / ${advisor.confidence}

预计一爆
$fcText
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
            when (resolveUiState(snapshot)) {
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

        refreshBtn.setOnClickListener { render() }

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
                Toast.makeText(context, "Need at least 2 roast history entries", Toast.LENGTH_SHORT).show()
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
                if (running) render()
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
暂无历史记录

保存第一锅后可启用参考
        """.trimIndent()

        return """
最近参考锅
${latest.batchId}

健康 / 评测
${latest.batchStatus} / ${if (latest.evaluation != null) "已保存" else "未保存"}
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
暂无历史记录

当前建议
保存第一锅后可启用偏差检查
            """.trimIndent()
        }

        val alerts = mutableListOf<String>()
        val currentElapsed = session.lastElapsedSec

        val lastYellow = latest.actualYellowSec ?: latest.predictedYellowSec
        val lastFc = latest.actualFcSec ?: latest.predictedFcSec

        val currentHealthScore = riskScore(currentHealth)
        val lastHealthScore = riskScore(latest.roastHealthHeadline)
        if (currentHealthScore > lastHealthScore && currentHealthScore > 0) {
            alerts += "当前健康状态弱于最近参考锅"
        }

        if (lastYellow != null && currentElapsed >= lastYellow + 20) {
            alerts += "当前节奏已慢于最近 yellow 参考"
        }

        if (lastFc != null && currentElapsed >= lastFc - 15) {
            alerts += "当前节奏已接近最近一爆参考"
        }

        val currentEnv = AppState.lastPlannerInput
        val currentEnvTemp = currentEnv?.envTemp
        val currentEnvRh = currentEnv?.envRH
        val envShiftDetected =
            currentEnvTemp != null &&
                currentEnvRh != null &&
                (abs(currentEnvTemp - latest.envTemp) >= 1.5 || abs(currentEnvRh - latest.envRh) >= 8.0)

        if (envShiftDetected) {
            alerts += "当前环境与最近参考锅差异明显"
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
${if (alerts.isEmpty()) "无明显参考偏差" else alerts.take(2).joinToString("\n")}
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
