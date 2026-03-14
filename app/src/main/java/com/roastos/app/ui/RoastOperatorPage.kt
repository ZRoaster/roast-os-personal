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

    private enum class OperatorUiState {
        DISCONNECTED,
        NOT_READY,
        ACTIVE
    }

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST OPERATOR"))
        root.addView(UiKit.pageSubtitle(context, "Connect, verify, observe, and act"))
        root.addView(UiKit.spacer(context))

        val stateCard = UiKit.card(context)
        val stateBody = UiKit.bodyText(context, "")
        stateCard.addView(UiKit.cardTitle(context, "OPERATOR STATE"))
        stateCard.addView(
            UiKit.helperText(
                context,
                "This page changes by state. First confirm connection, then readiness, then live operation."
            )
        )
        stateCard.addView(UiKit.spacer(context))
        stateCard.addView(UiKit.sectionLabel(context, "STATE"))
        stateCard.addView(stateBody)
        root.addView(stateCard)
        root.addView(UiKit.spacer(context))

        val primaryCard = UiKit.card(context)
        val primaryBody = UiKit.bodyText(context, "")
        primaryCard.addView(UiKit.cardTitle(context, "PRIMARY TASK"))
        primaryCard.addView(
            UiKit.helperText(
                context,
                "This block shows the most important next step under the current operator state."
            )
        )
        primaryCard.addView(UiKit.spacer(context))
        primaryCard.addView(UiKit.sectionLabel(context, "TASK"))
        primaryCard.addView(primaryBody)
        root.addView(primaryCard)
        root.addView(UiKit.spacer(context))

        val focusCard = UiKit.card(context)
        val focusBody = UiKit.bodyText(context, "")
        focusCard.addView(UiKit.cardTitle(context, "CURRENT FOCUS"))
        focusCard.addView(
            UiKit.helperText(
                context,
                "In active roast state, this becomes the main judgment and action area."
            )
        )
        focusCard.addView(UiKit.spacer(context))
        focusCard.addView(UiKit.sectionLabel(context, "FOCUS"))
        focusCard.addView(focusBody)
        root.addView(focusCard)
        root.addView(UiKit.spacer(context))

        val supportCard = UiKit.card(context)
        val supportBody = UiKit.bodyText(context, "")
        supportCard.addView(UiKit.cardTitle(context, "SUPPORT"))
        supportCard.addView(
            UiKit.helperText(
                context,
                "Use this area for readiness notes, prediction, or reference drift depending on current state."
            )
        )
        supportCard.addView(UiKit.spacer(context))
        supportCard.addView(UiKit.sectionLabel(context, "SUPPORT"))
        supportCard.addView(supportBody)
        root.addView(supportCard)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        val startBtn = UiKit.primaryButton(context, "START ROAST")
        val stopBtn = UiKit.secondaryButton(context, "STOP ROAST")
        val refreshBtn = UiKit.secondaryButton(context, "REFRESH")
        val openEnvironmentBtn = UiKit.secondaryButton(context, "OPEN ENVIRONMENT")
        val openStudioBtn = UiKit.secondaryButton(context, "OPEN STUDIO")
        val openRecentBtn = UiKit.secondaryButton(context, "OPEN RECENT ROASTS")
        val openLatestBtn = UiKit.secondaryButton(context, "OPEN LATEST HISTORY")
        val openLastCompareBtn = UiKit.secondaryButton(context, "OPEN LAST COMPARE")

        controlCard.addView(UiKit.cardTitle(context, "QUICK CONTROL"))
        controlCard.addView(
            UiKit.helperText(
                context,
                "Controls stay available, but their importance depends on current operator state."
            )
        )
        controlCard.addView(UiKit.spacer(context))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(openEnvironmentBtn)
        controlCard.addView(openStudioBtn)
        controlCard.addView(openRecentBtn)
        controlCard.addView(openLatestBtn)
        controlCard.addView(openLastCompareBtn)
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

            stateBody.text = """
当前状态
未连接 / 未确认

Session
${session.status}

Telemetry
${if (session.lastBeanTemp > 0.0 || session.lastElapsedSec > 0) "弱数据存在" else "未检测到有效实时数据"}

首页逻辑
先确认机器与数据，再进入烘焙操作。
            """.trimIndent()

            primaryBody.text = """
当前主任务
先确认连接状态。

下一步建议
1. 检查机器连接或数据来源
2. 进入 Environment 补全环境信息
3. 再返回本页确认是否进入可操作状态

当前不建议
不要把实时判断与动作建议当成可靠依据。
            """.trimIndent()

            focusBody.text = """
当前焦点
机器连接优先于烘焙判断。

只有当 telemetry 足够明确时，
Operate 首页才应该进入真正的操作模式。
            """.trimIndent()

            supportBody.text = """
可用入口
- OPEN ENVIRONMENT
- OPEN STUDIO
- OPEN RECENT ROASTS

说明
你现在更适合做准备工作，而不是读实时烘焙建议。
            """.trimIndent()

            startBtn.isEnabled = false
            stopBtn.isEnabled = false
        }

        fun renderNotReady(snapshot: RoastSessionBusSnapshot) {
            val session = snapshot.session
            val plannerInput = AppState.lastPlannerInput
            val hasEnvironment = plannerInput?.envTemp != null && plannerInput.envRH != null
            val environmentText = if (hasEnvironment) {
                "${oneDecimal(plannerInput!!.envTemp)} ℃ / ${oneDecimal(plannerInput.envRH)} %"
            } else {
                "缺失"
            }

            stateBody.text = """
当前状态
已连接但未就绪

Session
${session.status}

Environment
$environmentText

Telemetry
${if (session.lastBeanTemp > 0.0 || session.lastElapsedSec > 0) "已存在" else "偏弱"}

首页逻辑
系统已进入准备阶段，但还不适合完整主操作流。
            """.trimIndent()

            val readinessNote = when {
                !hasEnvironment -> "缺少环境输入，请先补全。"
                session.lastElapsedSec <= 0 -> "尚未进入有效烘焙进程。"
                else -> "当前数据存在，但还不适合完整操作流。"
            }

            primaryBody.text = """
当前主任务
把状态补到可操作。

当前缺口
$readinessNote

下一步建议
1. 先补环境或确认 session
2. 检查是否已经进入实际烘焙状态
3. 再进入完整操作判断
            """.trimIndent()

            val headline = RoastInsightBridge.observationHeadlineForSnapshot(snapshot)
            focusBody.text = """
当前观察
$headline

说明
这条观察现在只能作为弱提示，
还不应当作为完整操作依据。
            """.trimIndent()

            val latest = RoastHistoryEngine.latest()
            supportBody.text = """
最近参考
${latest?.batchId ?: "无"}

环境就绪
${if (hasEnvironment) "已补全" else "未补全"}

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
            val latest = RoastHistoryEngine.latest()

            stateBody.text = """
当前状态
烘焙中

状态
${session.status}

核心读数
BT   ${String.format("%.1f", session.lastBeanTemp)} ℃
RoR  ${String.format("%.1f", session.lastRor)} ℃/min
时间  ${formatElapsed(session.lastElapsedSec)}

阶段 / 健康
${snapshot.companion.phaseLabel} / ${buildHealthHeadline(snapshot.validation)}
            """.trimIndent()

            primaryBody.text = """
当前主任务
按当前阶段维持正确动作。

观察
${RoastInsightBridge.observationHeadlineForSnapshot(snapshot)}

动作
火力：${advisor.finalHeatAdvice}
风门：${advisor.finalAirflowAdvice}
            """.trimIndent()

            focusBody.text = """
阶段 / 优先级
${advisor.stage} / ${advisor.priority}

系统理解
${advisor.insightSummary}

风味方向
${advisor.flavorDirection}

风险 / 置信度
${advisor.riskLevel} / ${advisor.confidence}
            """.trimIndent()

            val currentHealth = buildHealthHeadline(snapshot.validation)
            val fcText = prediction.estimatedFirstCrackWindowSec?.let { formatElapsed(it) } ?: "-"
            val predictionRisk = prediction.predictedRisk
            val predictionReason = prediction.reason.ifBlank { "-" }

            val referenceText = if (latest == null) {
                "No roast history yet."
            } else {
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

                if (alerts.isEmpty()) {
                    "无明显参考偏差。"
                } else {
                    alerts.take(2).joinToString("\n")
                }
            }

            supportBody.text = """
当前健康
$currentHealth

预测风险
$predictionRisk

预计一爆
$fcText

预测说明
$predictionReason

参考预警
$referenceText
            """.trimIndent()

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

        openStudioBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
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

    private fun oneDecimal(value: Double): String {
        return String.format(java.util.Locale.getDefault(), "%.1f", value)
    }
}
