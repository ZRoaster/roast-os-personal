package com.roastos.app

data class RoastRiskPolicyResult(

    val allowContinue: Boolean,

    val warningLevel: String,

    val message: String,

    val recommendedHeatAdjust: Int?,

    val recommendedAirAdjust: Int?
)

object RoastRiskPolicyEngine {

    fun evaluateRisk(

        beanTemp: Double,
        ror: Double,
        phase: String

    ): RoastRiskPolicyResult {

        if (ror < 0.5 && phase == "development") {

            return RoastRiskPolicyResult(

                allowContinue = true,

                warningLevel = "watch",

                message =
                """
RoR 已接近 crash 区域

如果继续降低火力
可能出现风味塌陷

建议轻微补能
                """.trimIndent(),

                recommendedHeatAdjust = +5,

                recommendedAirAdjust = null
            )
        }

        if (ror > 25 && phase == "drying") {

            return RoastRiskPolicyResult(

                allowContinue = true,

                warningLevel = "medium",

                message =
                """
升温过快

可能导致 scorching
或 drying 过短

建议轻微降火
                """.trimIndent(),

                recommendedHeatAdjust = -5,

                recommendedAirAdjust = null
            )
        }

        if (beanTemp > 235) {

            return RoastRiskPolicyResult(

                allowContinue = false,

                warningLevel = "high",

                message =
                """
BT 已进入过热区域

继续加热存在烘废风险
建议立即降低火力
                """.trimIndent(),

                recommendedHeatAdjust = -10,

                recommendedAirAdjust = +10
            )
        }

        return RoastRiskPolicyResult(

            allowContinue = true,

            warningLevel = "none",

            message = "系统未检测到明显风险",

            recommendedHeatAdjust = null,

            recommendedAirAdjust = null
        )
    }
}
