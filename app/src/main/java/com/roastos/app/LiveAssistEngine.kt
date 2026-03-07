package com.roastos.app

data class LiveAdvice(
    val deviation: String,
    val heat: String,
    val airflow: String,
    val note: String
)

object LiveAssistEngine {

    fun turningAssist(predTurning:Int, actualTurning:Int): LiveAdvice {

        val diff = actualTurning - predTurning

        return when {
            diff > 8 -> LiveAdvice(
                "回温偏慢 ${diff}s",
                "H2 +60W",
                "1风门延后10s",
                "目标把Yellow拉回窗口"
            )

            diff < -8 -> LiveAdvice(
                "回温偏快 ${-diff}s",
                "H2 -60W",
                "1风门提前10s",
                "防止转黄过快"
            )

            else -> LiveAdvice(
                "回温正常",
                "保持原策略",
                "保持原策略",
                "继续观察ROR"
            )
        }
    }

    fun yellowAssist(predYellow:Int, actualYellow:Int, ror:Double): LiveAdvice {

        val diff = actualYellow - predYellow

        return when {

            diff > 15 -> LiveAdvice(
                "脱水偏慢",
                "H3 +60W",
                "2风门延后",
                "避免FC过晚"
            )

            diff < -15 -> LiveAdvice(
                "脱水偏快",
                "H3 -60W",
                "2风门提前",
                "避免爆前冲高"
            )

            ror > 14 -> LiveAdvice(
                "ROR偏高",
                "H3 -40W",
                "风门+2Pa",
                "压制梅纳冲高"
            )

            else -> LiveAdvice(
                "梅纳正常",
                "保持火力",
                "保持风门",
                "稳定推进"
            )
        }
    }

    fun fcAssist(predFc:Int, actualFc:Int, ror:Double): LiveAdvice {

        val diff = actualFc - predFc

        return when {

            ror > 10 -> LiveAdvice(
                "爆前ROR过高",
                "H4 -60W",
                "风门 +2Pa",
                "防止发展段冲高"
            )

            ror < 7 -> LiveAdvice(
                "爆前能量不足",
                "H4 +40W",
                "风门保持",
                "避免发展塌陷"
            )

            else -> LiveAdvice(
                "爆前状态正常",
                "保持火力",
                "保持风门",
                "发展控制75–90s"
            )
        }
    }
}
