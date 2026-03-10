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
                title = "Quiet",
                body = """
The roaster is still.

Nothing is unfolding right now.
When you begin, I will stay with the roast.
                """.trimIndent(),
                tone = "quiet",
                phaseLabel = phase,
                riskLevel = "none"
            )
        }

        if (elapsed < 20) {
            return RoastCompanionMessage(
                title = "Beginning",
                body = """
The roast has just started.

Do not rush to judge it yet.
Let the beans absorb heat and reveal their direction.
                """.trimIndent(),
                tone = "quiet",
                phaseLabel = phase,
                riskLevel = "low"
            )
        }

        if (beanTemp < 120) {
            return when {
                ror > 12 -> RoastCompanionMessage(
                    title = "Strong opening",
                    body = """
Energy is entering the roast with confidence.

This is a lively start.
Watch it, but do not interrupt too early.
                    """.trimIndent(),
                    tone = "supportive",
                    phaseLabel = phase,
                    riskLevel = "watch"
                )

                ror > 5 -> RoastCompanionMessage(
                    title = "Steady absorption",
                    body = """
The beans are quietly taking in heat.

Nothing feels urgent.
Stay present and let the roast settle into itself.
                    """.trimIndent(),
                    tone = "quiet",
                    phaseLabel = phase,
                    riskLevel = "low"
                )

                else -> RoastCompanionMessage(
                    title = "Low momentum",
                    body = """
The roast feels a little hesitant.

If this continues, it may lose shape later.
A small increase in energy could help it move more freely.
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
                    title = "Fast through drying",
                    body = """
The roast is moving quickly through drying.

That can be useful, but only if the structure stays calm.
Watch whether the rise remains clean instead of sharp.
                    """.trimIndent(),
                    tone = "observant",
                    phaseLabel = phase,
                    riskLevel = "watch"
                )

                ror > 6 -> RoastCompanionMessage(
                    title = "Good drying rhythm",
                    body = """
The roast feels balanced here.

Energy is present, but not noisy.
This is the kind of middle pace that often leaves room for clarity later.
                    """.trimIndent(),
                    tone = "quiet",
                    phaseLabel = phase,
                    riskLevel = "low"
                )

                else -> RoastCompanionMessage(
                    title = "Drying is slowing",
                    body = """
The movement is starting to thin out.

Not a crisis yet,
but it would be wise to watch whether momentum keeps fading before Maillard begins.
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
                    title = "Energetic Maillard",
                    body = """
The roast is carrying strong momentum into Maillard.

This can build sweetness and depth,
but only if you keep it from becoming harsh or impatient.
                    """.trimIndent(),
                    tone = "exploration",
                    phaseLabel = phase,
                    riskLevel = "watch"
                )

                ror > 5 -> RoastCompanionMessage(
                    title = "Calm development of structure",
                    body = """
This is a thoughtful middle section.

The roast is building shape without forcing itself.
Often this is where balance begins to become possible.
                    """.trimIndent(),
                    tone = "quiet",
                    phaseLabel = phase,
                    riskLevel = "low"
                )

                else -> RoastCompanionMessage(
                    title = "Structure may flatten",
                    body = """
Momentum is becoming fragile in the middle of the roast.

If it falls too far here,
the finish may feel short or empty.
A gentle lift in energy may preserve the cup's inner structure.
                    """.trimIndent(),
                    tone = "supportive",
                    phaseLabel = phase,
                    riskLevel = "medium"
                )
            }
        }

        return when {
            session.firstCrackLikely && !session.dropSuggested -> RoastCompanionMessage(
                title = "First crack is near",
                body = """
The roast is approaching its threshold.

This is not the moment to panic.
Listen carefully, stay soft, and let the finish become intentional.
                """.trimIndent(),
                tone = "quiet",
                phaseLabel = phase,
                riskLevel = "watch"
            )

            session.dropSuggested -> RoastCompanionMessage(
                title = "Finish is opening",
                body = """
The roast is ready to be decided.

You do not need more force now.
What matters is whether you want to preserve clarity or extend sweetness.
                """.trimIndent(),
                tone = "exploration",
                phaseLabel = phase,
                riskLevel = "medium"
            )

            ror <= 3 -> RoastCompanionMessage(
                title = "Finish is losing momentum",
                body = """
The roast feels heavy at the end.

If the line keeps softening,
the finish may blur.
Try to keep the ending alive rather than merely complete.
                """.trimIndent(),
                tone = "supportive",
                phaseLabel = phase,
                riskLevel = "medium"
            )

            else -> RoastCompanionMessage(
                title = "Development is underway",
                body = """
The roast has entered its final expression.

Stay close.
Small choices here will shape whether the cup feels clear, sweet, deep, or tired.
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
Companion
${message.title}

Voice
${message.body}

Phase
${message.phaseLabel}

Risk
${message.riskLevel}
        """.trimIndent()
    }
}
