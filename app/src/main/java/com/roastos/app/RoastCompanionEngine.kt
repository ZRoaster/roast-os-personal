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
        val elapsed = session.lastElapsedSec

        val phase = detectPhase(temp)

        val rorState = when {
            ror < 2 -> "RoR 低"
            ror < 6 -> "RoR 平稳"
            ror < 10 -> "RoR 良好"
            else -> "RoR 偏高"
        }

        val suggestion = buildSuggestion(phase, ror)

        val body =
            """
状态
${phaseObservation(phase)}

RoR
$rorState

建议
$suggestion
            """.trimIndent()

        return RoastCompanionMessage(
            title = phase,
            body = body,
            phaseLabel = phase,
            riskLevel = riskLevel(ror)
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

            "Charge" ->
                "豆子吸热阶段"

            "Drying" ->
                "水分蒸发进行中"

            "Maillard" ->
                "结构与香气正在形成"

            "First Crack" ->
                "进入爆裂区间"

            "Development" ->
                "发展阶段"

            else ->
                "观察曲线变化"
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
                if (ror < 4)
                    "轻微增加能量"
                else
                    "保持当前能量"

            "Maillard" ->
                if (ror < 4)
                    "避免能量下降"
                else
                    "维持稳定下降"

            "First Crack" ->
                "准备进入发展段"

            "Development" ->
                "控制收尾节奏"

            else ->
                "观察曲线"
        }
    }

    private fun riskLevel(
        ror: Double
    ): String {

        return when {

            ror < 1.5 -> "medium"

            ror > 12 -> "watch"

            else -> "low"
        }
    }
}
