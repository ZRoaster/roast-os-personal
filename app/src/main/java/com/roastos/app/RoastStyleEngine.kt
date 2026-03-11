package com.roastos.app

object RoastStyleEngine {

    private val builtInStyles = listOf(

        RoastStyleProfile(
            id = "nordic_clarity",
            name = "Nordic Clarity",
            description = "高透明度、花香、果汁感、短发展。",
            origin = "builtin",
            flavorGoal = "高酸质、高清晰度、花果香",
            suitableProcess = "Washed / Clean Natural",
            turningTargetSec = 60,
            yellowTargetSec = 240,
            firstCrackTargetSec = 480,
            dropTargetSec = 540,
            developmentRatio = 0.10,
            rorTrend = "Fast Declining",
            airflowStrategy = "Early Open",
            drumStrategy = "Moderate",
            notes = "适合强调透明感和风味分离度。"
        ),

        RoastStyleProfile(
            id = "modern_balance",
            name = "Modern Balance",
            description = "现代精品平衡风格，兼顾甜感、层次和清晰度。",
            origin = "builtin",
            flavorGoal = "平衡、甜感、层次",
            suitableProcess = "Washed / Natural / Honey",
            turningTargetSec = 65,
            yellowTargetSec = 255,
            firstCrackTargetSec = 510,
            dropTargetSec = 585,
            developmentRatio = 0.13,
            rorTrend = "Stable Declining",
            airflowStrategy = "Progressive",
            drumStrategy = "Moderate",
            notes = "适合作为大多数精品豆的通用起点。"
        ),

        RoastStyleProfile(
            id = "clean_precision",
            name = "Clean Precision",
            description = "强调干净、精确、风味边界清楚的风格。",
            origin = "builtin",
            flavorGoal = "干净、分离、精确",
            suitableProcess = "Washed",
            turningTargetSec = 60,
            yellowTargetSec = 250,
            firstCrackTargetSec = 500,
            dropTargetSec = 570,
            developmentRatio = 0.12,
            rorTrend = "Stable",
            airflowStrategy = "Clean Exhaust",
            drumStrategy = "Stable",
            notes = "适合追求极高干净度与精确表达。"
        ),

        RoastStyleProfile(
            id = "sweet_body",
            name = "Sweet Body",
            description = "强调甜感、醇厚度和中后段支撑。",
            origin = "builtin",
            flavorGoal = "甜、圆润、厚度",
            suitableProcess = "Natural / Honey / Espresso Lots",
            turningTargetSec = 70,
            yellowTargetSec = 270,
            firstCrackTargetSec = 525,
            dropTargetSec = 620,
            developmentRatio = 0.16,
            rorTrend = "Gentle Declining",
            airflowStrategy = "Moderate-Low Early",
            drumStrategy = "Supportive",
            notes = "适合想要 body 和甜感更明显的风格。"
        ),

        RoastStyleProfile(
            id = "classic_espresso",
            name = "Classic Espresso",
            description = "传统浓缩导向，强调厚重、坚果、巧克力感。",
            origin = "builtin",
            flavorGoal = "低酸、浓厚、巧克力坚果",
            suitableProcess = "Espresso Lots / Blend",
            turningTargetSec = 75,
            yellowTargetSec = 285,
            firstCrackTargetSec = 540,
            dropTargetSec = 660,
            developmentRatio = 0.18,
            rorTrend = "Controlled Low",
            airflowStrategy = "Moderate",
            drumStrategy = "Supportive",
            notes = "适合传统浓缩与奶咖结构。"
        ),

        RoastStyleProfile(
            id = "exploration_mode",
            name = "Exploration Mode",
            description = "允许风格探索，不预设唯一风味终点。",
            origin = "builtin",
            flavorGoal = "探索新结构与新风味",
            suitableProcess = "Any",
            turningTargetSec = null,
            yellowTargetSec = null,
            firstCrackTargetSec = null,
            dropTargetSec = null,
            developmentRatio = null,
            rorTrend = "Adaptive",
            airflowStrategy = "Experimental",
            drumStrategy = "Experimental",
            notes = "用于保留探索自由度，由 AI 负责守边界。"
        )
    )

    fun allBuiltIn(): List<RoastStyleProfile> {
        return builtInStyles
    }

    fun findById(id: String): RoastStyleProfile? {
        return builtInStyles.firstOrNull { it.id == id }
    }

    fun defaultStyle(): RoastStyleProfile {
        return builtInStyles.first { it.id == "modern_balance" }
    }

    fun recommendForSnapshot(
        snapshot: RoastSessionBusSnapshot
    ): RoastStyleProfile {

        val log = snapshot.log
        val cup = RoastCupProfileEngine.evaluate(log)
        val health = snapshot.validation

        if (health.hasIssues()) {
            return findById("exploration_mode") ?: defaultStyle()
        }

        return when {
            cup.clarityLevel == "高" && cup.acidityLevel == "明亮" ->
                findById("nordic_clarity") ?: defaultStyle()

            cup.clarityLevel == "高" && cup.sweetnessLevel == "中" ->
                findById("clean_precision") ?: defaultStyle()

            cup.bodyLevel == "厚" && cup.sweetnessLevel == "高" ->
                findById("sweet_body") ?: defaultStyle()

            cup.roastStyle == "中深烘" ->
                findById("classic_espresso") ?: defaultStyle()

            else ->
                defaultStyle()
        }
    }

    fun buildDisplayText(
        profile: RoastStyleProfile
    ): String {
        return """
风格
${profile.name}

描述
${profile.description}

风味目标
${profile.flavorGoal}

适用处理
${profile.suitableProcess ?: "-"}

Turning
${profile.turningTargetSec?.let { formatSec(it) } ?: "-"}

Yellow
${profile.yellowTargetSec?.let { formatSec(it) } ?: "-"}

First Crack
${profile.firstCrackTargetSec?.let { formatSec(it) } ?: "-"}

Drop
${profile.dropTargetSec?.let { formatSec(it) } ?: "-"}

Development
${profile.developmentRatio?.let { "${String.format("%.1f", it * 100)}%" } ?: "-"}

RoR
${profile.rorTrend ?: "-"}

风门策略
${profile.airflowStrategy ?: "-"}

转速策略
${profile.drumStrategy ?: "-"}

备注
${profile.notes ?: "-"}
        """.trimIndent()
    }

    fun buildRecommendedStyleText(
        snapshot: RoastSessionBusSnapshot
    ): String {
        val profile = recommendForSnapshot(snapshot)
        return buildDisplayText(profile)
    }

    private fun formatSec(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }
}
