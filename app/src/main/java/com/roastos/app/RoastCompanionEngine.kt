package com.roastos.app

data class RoastCompanionMessage(
    val title: String,
    val body: String,
    val tone: String,
    val phaseLabel: String,
    val riskLevel: String
)

object RoastCompanionEngine {

    fun buildMessage(
        session: RoastSessionState
    ): RoastCompanionMessage {

        val beanTemp = session.lastBeanTemp
        val ror = session.lastRor
        val elapsed = session.lastElapsedSec
        val phase = RoastSessionEngine.phaseLabel(session.phase)

        if (session.status != RoastSessionStatus.RUNNING) {
            return RoastCompanionMessage(
                title = "安静",
                body = """
现在还没有开始。

机器是静的，
豆子也还没有进入它自己的时间。

等你按下开始，
我会陪着这一锅一起走。
                """.trimIndent(),
                tone = "quiet",
                phaseLabel = phase,
                riskLevel = "none"
            )
        }

        if (elapsed < 20) {
            return RoastCompanionMessage(
                title = "刚刚开始",
                body = """
这只是开头。

先不要急着判断这锅会走向哪里。
让豆子先吸热，
让节奏自己显出来。
                """.trimIndent(),
                tone = "quiet",
                phaseLabel = phase,
                riskLevel = "low"
            )
        }

        if (beanTemp < 120) {
            return when {
                ror > 12 -> RoastCompanionMessage(
                    title = "开局有力",
                    body = """
这一锅起势很明显。

热量进去得很积极，
现在还不用急着压它。
先看它是不是能继续保持干净，而不是变得急躁。
                    """.trimIndent(),
                    tone = "supportive",
                    phaseLabel = phase,
                    riskLevel = "watch"
                )

                ror > 5 -> RoastCompanionMessage(
                    title = "吸热平稳",
                    body = """
豆子正在安静地接住热量。

现在没有什么需要立刻处理的。
只要继续看着它，
让这一锅慢慢站稳。
                    """.trimIndent(),
                    tone = "quiet",
                    phaseLabel = phase,
                    riskLevel = "low"
                )

                else -> RoastCompanionMessage(
                    title = "起势偏弱",
                    body = """
这一锅现在有一点犹豫。

还不到危险的时候，
但如果这种迟疑继续下去，
后面可能会变得不够舒展。

也许可以给一点点能量，
让它重新动起来。
                    """.trimIndent(),
                    tone = "supportive",
                    phaseLabel = phase,
                    riskLevel = "watch"
                )
            }
        }

        if (beanTemp < 160) {
            return when {
                ror > 11 -> RoastCompanionMessage(
                    title = "脱水推进偏快",
                    body = """
这一段走得有点快。

快本身不是问题，
问题在于它会不会因此失去从容。

你现在要看的，
不是数字漂亮不漂亮，
而是这条线还干不干净。
                    """.trimIndent(),
                    tone = "observant",
                    phaseLabel = phase,
                    riskLevel = "watch"
                )

                ror > 6 -> RoastCompanionMessage(
                    title = "节奏是好的",
                    body = """
这一段很稳。

热量在推进，
但没有吵闹，
也没有发虚。

这种中段的平衡，
往往会给后面的清晰感留下空间。
                    """.trimIndent(),
                    tone = "quiet",
                    phaseLabel = phase,
                    riskLevel = "low"
                )

                else -> RoastCompanionMessage(
                    title = "中段在变薄",
                    body = """
这一段的动能开始变轻了。

现在还不用急着下结论，
但要留意它会不会在进入梅纳前就先失去支撑。

如果继续往下掉，
后段可能会变得空。
                    """.trimIndent(),
                    tone = "supportive",
                    phaseLabel = phase,
                    riskLevel = "watch"
                )
            }
        }

        if (beanTemp < 190) {
            return when {
                ror > 10 -> RoastCompanionMessage(
                    title = "梅纳反应很活跃",
                    body = """
这一锅带着不小的动能进入中后段。

这可能会带来甜感和厚度，
但前提是它别变得粗糙。

你现在要做的，
不是立刻干预，
而是看它还能不能保持细致。
                    """.trimIndent(),
                    tone = "exploration",
                    phaseLabel = phase,
                    riskLevel = "watch"
                )

                ror > 5 -> RoastCompanionMessage(
                    title = "结构正在形成",
                    body = """
这一段很像是在慢慢长出骨架。

没有硬冲，
也没有塌下去。

很多平衡感，
其实就是在这种不喧哗的中段里决定的。
                    """.trimIndent(),
                    tone = "quiet",
                    phaseLabel = phase,
                    riskLevel = "low"
                )

                else -> RoastCompanionMessage(
                    title = "中段支撑不足",
                    body = """
这一锅的内部张力有点弱了。

如果这里继续失速，
后面的尾段可能会显得短，或者发闷。

不用很大动作，
但也许需要轻轻托它一下。
                    """.trimIndent(),
                    tone = "supportive",
                    phaseLabel = phase,
                    riskLevel = "medium"
                )
            }
        }

        return when {
            session.firstCrackLikely && !session.dropSuggested -> RoastCompanionMessage(
                title = "快到一爆了",
                body = """
门快开了。

这不是慌的时候，
是把注意力收回来的一刻。

安静一点，
仔细听，
让这锅自己把边界说出来。
                """.trimIndent(),
                tone = "quiet",
                phaseLabel = phase,
                riskLevel = "watch"
            )

            session.dropSuggested -> RoastCompanionMessage(
                title = "尾段已经打开",
                body = """
现在不是继续用力的时候了。

这一刻更像是在决定：
你想把清晰留住，
还是想把甜感再往前送一点。

答案不会自己跳出来，
但它已经在你手边了。
                """.trimIndent(),
                tone = "exploration",
                phaseLabel = phase,
                riskLevel = "medium"
            )

            ror <= 3 -> RoastCompanionMessage(
                title = "尾段有点发沉",
                body = """
这一锅在尾段显得有些重了。

如果这条线继续软下去，
收尾可能会模糊。

现在更重要的，
不是把它做完，
而是让它活着结束。
                """.trimIndent(),
                tone = "supportive",
                phaseLabel = phase,
                riskLevel = "medium"
            )

            else -> RoastCompanionMessage(
                title = "发展已经开始",
                body = """
这一锅正在进入它最后的表达。

这一段里，
很小的选择都会留下痕迹。

不用急，
但要靠近它。
                """.trimIndent(),
                tone = "observant",
                phaseLabel = phase,
                riskLevel = "watch"
            )
        }
    }

    fun buildDisplayText(
        session: RoastSessionState
    ): String {
        val message = buildMessage(session)

        return """
陪伴
${message.title}

声音
${message.body}

阶段
${message.phaseLabel}

风险
${formatRisk(message.riskLevel)}
        """.trimIndent()
    }

    private fun formatRisk(risk: String): String {
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
