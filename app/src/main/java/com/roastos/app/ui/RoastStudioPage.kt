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
        val openEnvironmentBtn = UiKit.secondaryButton(context, "OPEN ENVIRONMENT")
        val openRecentBtn = UiKit.secondaryButton(context, "OPEN RECENT ROASTS")
        val openLatestBtn = UiKit.secondaryButton(context, "OPEN LATEST HISTORY")
        val openKnowledgeBtn = UiKit.secondaryButton(context, "OPEN KNOWLEDGE")
        val openStylesBtn = UiKit.secondaryButton(context, "OPEN STYLES")
        val openMyStylesBtn = UiKit.secondaryButton(context, "OPEN MY STYLES")
        val openExperimentsBtn = UiKit.secondaryButton(context, "OPEN EXPERIMENTS")
        val openKnowledgeEvolutionBtn = UiKit.secondaryButton(context, "OPEN KNOWLEDGE EVOLUTION")

        controlCard.addView(UiKit.cardTitle(context, "CONTROL"))
        controlCard.addView(startBtn)
        controlCard.addView(stopBtn)
        controlCard.addView(refreshBtn)
        controlCard.addView(openEnvironmentBtn)
        controlCard.addView(openRecentBtn)
        controlCard.addView(openLatestBtn)
        controlCard.addView(openKnowledgeBtn)
        controlCard.addView(openStylesBtn)
        controlCard.addView(openMyStylesBtn)
        controlCard.addView(openExperimentsBtn)
        controlCard.addView(openKnowledgeEvolutionBtn)

        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val styleCard = UiKit.card(context)
        val stylePanel = RoastStylePanel(context)
        styleCard.addView(UiKit.cardTitle(context, "STYLE"))
        styleCard.addView(stylePanel)
        root.addView(styleCard)
        root.addView(UiKit.spacer(context))

        val healthCard = UiKit.card(context)
        val healthBody = UiKit.bodyText(context, "")
        healthCard.addView(UiKit.cardTitle(context, "ROAST HEALTH"))
        healthCard.addView(healthBody)
        root.addView(healthCard)
        root.addView(UiKit.spacer(context))

        val decisionCard = UiKit.card(context)
        val decisionBody = UiKit.bodyText(context, "")
        decisionCard.addView(UiKit.cardTitle(context, "DECISION"))
        decisionCard.addView(decisionBody)
        root.addView(decisionCard)
        root.addView(UiKit.spacer(context))

        val advisorCard = UiKit.card(context)
        val advisorPanel = RoastControlAdvisorPanel(context)
        advisorCard.addView(UiKit.cardTitle(context, "CONTROL ADVISOR"))
        advisorCard.addView(advisorPanel)
        root.addView(advisorCard)
        root.addView(UiKit.spacer(context))

        val rorPredictionCard = UiKit.card(context)
        val rorPredictionPanel = RoastRorPredictionPanel(context)
        rorPredictionCard.addView(UiKit.cardTitle(context, "ROR PREDICTION"))
        rorPredictionCard.addView(rorPredictionPanel)
        root.addView(rorPredictionCard)
        root.addView(UiKit.spacer(context))

        val companionCard = UiKit.card(context)
        val companionBody = UiKit.bodyText(context, "")
        companionCard.addView(UiKit.cardTitle(context, "COMPANION"))
        companionCard.addView(companionBody)
        root.addView(companionCard)
        root.addView(UiKit.spacer(context))

        val phaseCard = UiKit.card(context)
        val phaseBody = UiKit.bodyText(context, "")
        phaseCard.addView(UiKit.cardTitle(context, "PHASE"))
        phaseCard.addView(phaseBody)
        root.addView(phaseCard)
        root.addView(UiKit.spacer(context))

        val riskCard = UiKit.card(context)
        val riskPanel = RoastRiskEventPanel(context)
        riskCard.addView(UiKit.cardTitle(context, "RISK EVENTS"))
        riskCard.addView(riskPanel)
        root.addView(riskCard)
        root.addView(UiKit.spacer(context))

        val experimentCard = UiKit.card(context)
        val experimentPanel = RoastExperimentPanel(context)
        experimentCard.addView(UiKit.cardTitle(context, "EXPERIMENT"))
        experimentCard.addView(experimentPanel)
        root.addView(experimentCard)
        root.addView(UiKit.spacer(context))

        val experimentLearningCard = UiKit.card(context)
        val experimentLearningPanel = RoastExperimentLearningPanel(context)
        experimentLearningCard.addView(UiKit.cardTitle(context, "EXPERIMENT LEARNING"))
        experimentLearningCard.addView(experimentLearningPanel)
        root.addView(experimentLearningCard)
        root.addView(UiKit.spacer(context))

        val experimentKnowledgeCard = UiKit.card(context)
        val experimentKnowledgePanel = RoastExperimentKnowledgePanel(context)
        experimentKnowledgeCard.addView(UiKit.cardTitle(context, "EXPERIMENT KNOWLEDGE"))
        experimentKnowledgeCard.addView(experimentKnowledgePanel)
        root.addView(experimentKnowledgeCard)
        root.addView(UiKit.spacer(context))

        val knowledgeEvolutionCard = UiKit.card(context)
        val knowledgeEvolutionPanel = RoastKnowledgeEvolutionPanel(context)
        knowledgeEvolutionCard.addView(UiKit.cardTitle(context, "KNOWLEDGE EVOLUTION"))
        knowledgeEvolutionCard.addView(knowledgeEvolutionPanel)
        root.addView(knowledgeEvolutionCard)
        root.addView(UiKit.spacer(context))

        val knowledgePromotionCard = UiKit.card(context)
        val knowledgePromotionPanel = RoastKnowledgePromotionPanel(context)
        knowledgePromotionCard.addView(UiKit.cardTitle(context, "KNOWLEDGE PROMOTION"))
        knowledgePromotionCard.addView(knowledgePromotionPanel)
        root.addView(knowledgePromotionCard)
        root.addView(UiKit.spacer(context))

        val machineDynamicsCard = UiKit.card(context)
        val machineDynamicsPanel = MachineDynamicsPanel(context)
        machineDynamicsCard.addView(UiKit.cardTitle(context, "MACHINE DYNAMICS"))
        machineDynamicsCard.addView(machineDynamicsPanel)
        root.addView(machineDynamicsCard)
        root.addView(UiKit.spacer(context))

        val environmentCard = UiKit.card(context)
        val environmentPanel = EnvironmentProfilePanel(context)
        environmentCard.addView(UiKit.cardTitle(context, "ENVIRONMENT"))
        environmentCard.addView(environmentPanel)
        root.addView(environmentCard)
        root.addView(UiKit.spacer(context))

        val environmentCompensationCard = UiKit.card(context)
        val environmentCompensationPanel = EnvironmentCompensationPanel(context)
        environmentCompensationCard.addView(UiKit.cardTitle(context, "ENVIRONMENT COMPENSATION"))
        environmentCompensationCard.addView(environmentCompensationPanel)
        root.addView(environmentCompensationCard)
        root.addView(UiKit.spacer(context))

        val controlModelCard = UiKit.card(context)
        val controlModelPanel = RoastControlModelPanel(context)
        controlModelCard.addView(UiKit.cardTitle(context, "CONTROL MODEL"))
        controlModelCard.addView(controlModelPanel)
        root.addView(controlModelCard)
        root.addView(UiKit.spacer(context))

        val aiContextCard = UiKit.card(context)
        val aiContextPanel = RoastAiContextPreviewPanel(context)
        aiContextCard.addView(UiKit.cardTitle(context, "AI CONTEXT"))
        aiContextCard.addView(aiContextPanel)
        root.addView(aiContextCard)
        root.addView(UiKit.spacer(context))

        val aiPromptCard = UiKit.card(context)
        val aiPromptPanel = RoastAiPromptPreviewPanel(context)
        aiPromptCard.addView(UiKit.cardTitle(context, "AI PROMPT"))
        aiPromptCard.addView(aiPromptPanel)
        root.addView(aiPromptCard)
        root.addView(UiKit.spacer(context))

        val aiAssistantCard = UiKit.card(context)
        val aiAssistantPanel = RoastAiAssistantPanel(context)
        aiAssistantCard.addView(UiKit.cardTitle(context, "AI ASSISTANT"))
        aiAssistantCard.addView(aiAssistantPanel)
        root.addView(aiAssistantCard)
        root.addView(UiKit.spacer(context))

        val explorationCard = UiKit.card(context)
        val explorationPanel = RoastExplorationPanel(context)
        explorationCard.addView(UiKit.cardTitle(context, "EXPLORATION"))
        explorationCard.addView(explorationPanel)
        root.addView(explorationCard)
        root.addView(UiKit.spacer(context))

        val learningCard = UiKit.card(context)
        val learningPanel = RoastLearningPanel(context)
        learningCard.addView(UiKit.cardTitle(context, "LEARNING"))
        learningCard.addView(learningPanel)
        root.addView(learningCard)
        root.addView(UiKit.spacer(context))

        val knowledgeCard = UiKit.card(context)
        val knowledgePanel = RoastKnowledgePanel(context)
        knowledgeCard.addView(UiKit.cardTitle(context, "KNOWLEDGE"))
        knowledgeCard.addView(knowledgePanel)
        root.addView(knowledgeCard)
        root.addView(UiKit.spacer(context))

        val logCard = UiKit.card(context)
        val logBody = UiKit.bodyText(context, "")
        logCard.addView(UiKit.cardTitle(context, "ROAST LOG"))
        logCard.addView(logBody)
        root.addView(logCard)
        root.addView(UiKit.spacer(context))

        val cupCard = UiKit.card(context)
        val cupBody = UiKit.bodyText(context, "")
        cupCard.addView(UiKit.cardTitle(context, "CUP PROFILE"))
        cupCard.addView(cupBody)
        root.addView(cupCard)
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
            val cup = RoastCupProfileEngine.evaluate(snapshot.log)

            overviewBody.text =
                """
STATUS   ${session.status}

BT       ${String.format("%.1f", session.lastBeanTemp)} ℃

RoR      ${String.format("%.1f", session.lastRor)} ℃/min

TIME     ${formatElapsed(session.lastElapsedSec)}

HEALTH   ${buildHealthHeadline(snapshot.validation)}
                """.trimIndent()

            stylePanel.update()

            healthBody.text = buildHealthText(snapshot.validation)
            decisionBody.text = buildDecisionPanel(decision)
            advisorPanel.update()
            rorPredictionPanel.update()

            companionBody.text =
                """
${snapshot.companion.title}

${snapshot.companion.body}

PHASE
${snapshot.companion.phaseLabel}

RISK
${formatRisk(snapshot.companion.riskLevel)}
                """.trimIndent()

            phaseBody.text = buildPhasePanel(snapshot)

            riskPanel.update()
            experimentPanel.update()
            experimentLearningPanel.update()
            experimentKnowledgePanel.update()
            knowledgeEvolutionPanel.update()
            knowledgePromotionPanel.update()
            machineDynamicsPanel.update()
            environmentPanel.update()
            environmentCompensationPanel.update()
            controlModelPanel.update()

            RoastAiSessionEngine.reset()
            RoastAiSessionEngine.build()

            aiContextPanel.update()
            aiPromptPanel.update()
            aiAssistantPanel.update()

            explorationPanel.update()
            learningPanel.update()
            knowledgePanel.update()

            logBody.text = snapshot.log.summary

            cupBody.text =
                """
风味预测
${cup.flavorPrediction}

推荐冲煮
${cup.brewMethod}

水温
${cup.brewTempC} ℃

粉水比
${cup.brewRatio}

研磨
${cup.grindLevel}

说明
${cup.brewNote}
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

        openEnvironmentBtn.setOnClickListener {
            EnvironmentInputPage.show(context, container)
        }

        openRecentBtn.setOnClickListener {
            RecentRoastListPage.show(context, container)
        }

        openLatestBtn.setOnClickListener {
            HistoryDetailPage.show(
                context = context,
                container = container,
                entry = RoastHistoryEngine.latest()
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

        openExperimentsBtn.setOnClickListener {
            RoastExperimentPage.show(context, container)
        }

        openKnowledgeEvolutionBtn.setOnClickListener {
            RoastKnowledgeEvolutionPage.show(context, container)
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

    private fun buildDecisionPanel(
        decision: RoastDecision
    ): String {
        return """
阶段
${decision.stage}

当前重点
${decision.priority}

火力建议
${decision.heatAction}

风门建议
${decision.airflowAction}

风味走向
${decision.flavorDirection}

可信度
${decision.confidence}

判断依据
${decision.rationale}
        """.trimIndent()
    }

    private fun buildPhasePanel(
        snapshot: RoastSessionBusSnapshot
    ): String {
        val p = snapshot.phaseState

        return """
CURRENT
${snapshot.companion.phaseLabel}

TURNING   ${formatPhaseEvent(p.turningPoint)}
DRY END   ${formatPhaseEvent(p.dryEnd)}
MAILLARD  ${formatPhaseEvent(p.maillardStart)}
FC        ${formatPhaseEvent(p.firstCrack)}
DROP      ${formatPhaseEvent(p.drop)}
        """.trimIndent()
    }

    private fun buildRecent(
        list: List<RoastHistoryEntry>
    ): String {
        if (list.isEmpty()) {
            return "No roast history yet."
        }

        return list.joinToString("\n\n────────\n\n") {
            """
BATCH
${it.batchId}

STATUS
${it.batchStatus}

HEALTH
${it.roastHealthHeadline}

TIME
${formatTime(it.createdAtMillis)}
            """.trimIndent()
        }
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

    private fun buildHealthText(
        validation: RoastValidationResult
    ): String {
        if (!validation.hasIssues()) {
            return """
状态
稳定

说明
当前未检测到明显风险
            """.trimIndent()
        }

        return validation.issues.joinToString("\n\n") {
            """
${it.title}
${it.detail}

等级
${formatRisk(it.severity)}
            """.trimIndent()
        }
    }

    private fun formatPhaseEvent(
        event: RoastPhaseEvent?
    ): String {
        if (event == null) return "-"
        return "${formatElapsed(event.elapsedSec)} · ${String.format("%.1f", event.beanTemp)}℃"
    }

    private fun formatElapsed(
        sec: Int
    ): String {
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatTime(
        ms: Long
    ): String {
        val t = ms / 1000
        val m = t / 60
        val s = t % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatRisk(
        risk: String
    ): String {
        return when (risk) {
            "none" -> "无"
            "low" -> "低"
            "watch" -> "留意"
            "medium" -> "中"
            "high" -> "高"
            else -> risk
        }
    }
}
