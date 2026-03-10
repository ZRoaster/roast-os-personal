package com.roastos.app

data class RoastCompanionMessage(
    val title: String,
    val body: String,
    val phaseLabel: String,
    val riskLevel: String
)

object RoastCompanionEngine {

    fun buildMessage(
        session: RoastSessionState
    ): RoastCompanionMessage {

        val temp = session.lastBeanTemp
        val ror = session.lastRor
        val phase = detectPhase(temp)

        val baseMessage = buildBaseMessage(
            session = session,
            phase = phase,
            ror = ror
        )

        val validation = RoastSessionValidator.validate(
            RoastSessionBusSnapshot(
                session = session,
                companion = RoastCompanionMessage(
                    title = baseMessage.title,
                    body = baseMessage.body,
                    phaseLabel = baseMessage.phaseLabel,
                    riskLevel = baseMessage.riskLevel
                ),
                log = RoastLogEngine.buildLog(session),
                phaseSummary = RoastPhaseDetectionEngine.summary(),
                logText = RoastLogEngine.buildLogText(session),
                historySummary = RoastHistoryEngine.summary(),
                validation = RoastValidationResult(emptyList())
            )
        )

        if (!validation.hasIssues()) {
            return baseMessage
        }

        val topIssue = validation.issues.first()

        return when (topIssue.code) {

            "stall" -> RoastCompanionMessage(
                title = phase,
                body = """
状态
动能不足

RoR
下降过慢并接近失速

建议
轻微增加能量，避免中后段发闷
                """.trimIndent(),
                phaseLabel = phase,
                riskLevel = "medium"
            )

            "crash" -> RoastCompanionMessage(
                title = phase,
                body = """
状态
尾段塌陷风险

RoR
后段掉得过快

建议
不要让收尾继续失去支撑，留意发展段是否变空
                """.trimIndent(),
                phaseLabel = phase,
                riskLevel = "high"
            )

            "flick" -> RoastCompanionMessage(
                title = phase,
                body = """
状态
尾段过冲风险

RoR
后段反弹偏强

建议
收尾不要再推，避免风味变尖
                """.trimIndent(),
                phaseLabel = phase,
                riskLevel = "medium"
            )

            "low_energy" -> RoastCompanionMessage(
                title = phase,
                body = """
状态
整体能量偏低

RoR
中段支撑不足

建议
观察是否需要小幅补能量，避免结构变薄
                """.trimIndent(),
                phaseLabel = phase,
                riskLevel = "watch"
            )

            "high_energy" -> RoastCompanionMessage(
                title = phase,
                body = """
状态
整体能量偏高

RoR
中段推进偏强

建议
保持克制，避免后面变粗或失去细致度
                """.trimIndent(),
                phaseLabel = phase,
                riskLevel = "watch"
            )

            else -> baseMessage
        }
    }

    private fun buildBaseMessage(
        session: RoastSessionState,
        phase: String,
        ror: Double
    ): RoastCompanionMessage {

        val body =
            """
状态
${phaseObservation(phase)}

RoR
${rorState(ror)}

建议
${buildSuggestion(phase, ror)}
            """.trimIndent()

        return RoastCompanionMessage(
            title = phase,
            body = body,
            phaseLabel = phase,
            riskLevel = baseRiskLevel(ror)
        )
    }

    private fun detectPhase(temp: Double): String {
        return when {
            temp < 110 -> "Charge"
            temp < 150 -> "Drying"
            temp < 175 -> "Maillard"
            temp < 200 -> "First Crack"
            else -> "Development"
        }
    }

    private fun phaseObservation(phase: String): String {
        return when (phase) {
            "Charge" -> "豆子吸热阶段"
            "Drying" -> "水分蒸发进行中"
            "Maillard" -> "结构与香气正在形成"
            "First Crack" -> "进入爆裂区间"
            "Development" -> "发展阶段"
            else -> "观察曲线变化"
        }
    }

    private fun rorState(ror: Double): String {
        return when {
            ror < 2 -> "RoR 低"
            ror < 6 -> "RoR 平稳"
            ror < 10 -> "RoR 良好"
            else -> "RoR 偏高"
        }
    }

    private fun buildSuggestion(
        phase: String,
        ror: Double
    ): String {
        return when (phase) {
            "Charge" ->
                "等待回温点"

            "Drying" ->
                if (ror < 4) "轻微增加能量" else "保持当前能量"

            "Maillard" ->
                if (ror < 4) "避免能量下降" else "维持稳定下降"

            "First Crack" ->
                "准备进入发展段"

            "Development" ->
                "控制收尾节奏"

            else ->
                "观察曲线"
        }
    }

    private fun baseRiskLevel(
        ror: Double
    ): String {
        return when {
            ror < 1.5 -> "medium"
            ror > 12 -> "watch"
            else -> "low"
        }
    }
}
