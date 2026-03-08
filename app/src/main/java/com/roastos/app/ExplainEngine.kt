package com.roastos.app

data class ExplainOutput(
    val why: String,
    val impact: String,
    val learning: String
)

object ExplainEngine {

    fun explain(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        actualTurning: Int?,
        actualYellow: Int?,
        actualFc: Int?,
        ror: Double?
    ): ExplainOutput {

        if (actualFc != null && ror != null) {

            return when {
                ror > 10.0 -> ExplainOutput(
                    why = "Pre-FC ROR is above target window.",
                    impact = "Development may overshoot and produce sharp finish.",
                    learning = "Control ROR before FC to keep development stable."
                )

                ror < 7.0 -> ExplainOutput(
                    why = "Energy entering development is too low.",
                    impact = "Risk of crash and hollow cup structure.",
                    learning = "Maintain enough heat before FC to support development."
                )

                else -> ExplainOutput(
                    why = "Development energy is within normal range.",
                    impact = "Finish likely to stay close to plan.",
                    learning = "Stable ROR through development improves sweetness."
                )
            }
        }

        if (actualYellow != null) {

            val diff = actualYellow - predYellow

            return when {
                diff > 15 -> ExplainOutput(
                    why = "Yellow arrived later than planned.",
                    impact = "FC may arrive late and cup may taste flat.",
                    learning = "Weak mid-phase energy delays Maillard progression."
                )

                diff < -15 -> ExplainOutput(
                    why = "Yellow arrived earlier than planned.",
                    impact = "FC may accelerate and create ROR spike.",
                    learning = "Too much early heat compresses Maillard stage."
                )

                else -> ExplainOutput(
                    why = "Yellow timing is close to plan.",
                    impact = "Maillard phase should stay balanced.",
                    learning = "Balanced drying stage leads to stable mid-phase."
                )
            }
        }

        if (actualTurning != null) {

            val diff = actualTurning - predTurning

            return when {
                diff > 8 -> ExplainOutput(
                    why = "Turning happened later than predicted.",
                    impact = "Drying stage may be weak.",
                    learning = "Front-end energy determines drying speed."
                )

                diff < -8 -> ExplainOutput(
                    why = "Turning happened earlier than predicted.",
                    impact = "Early acceleration may occur.",
                    learning = "Too much charge energy compresses drying."
                )

                else -> ExplainOutput(
                    why = "Turning timing is stable.",
                    impact = "Drying phase is on track.",
                    learning = "Stable turning indicates balanced charge energy."
                )
            }
        }

        return ExplainOutput(
            why = "Waiting for first roast anchor.",
            impact = "System will evaluate once turning is recorded.",
            learning = "First anchor determines drying phase trajectory."
        )
    }
}
