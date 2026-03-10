package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.*

object AiRoastAssistantPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(
            UiKit.pageTitle(
                context,
                "ROAST COMPANION"
            )
        )

        root.addView(
            UiKit.pageSubtitle(
                context,
                "Observation driven roast insight"
            )
        )

        root.addView(UiKit.spacer(context))

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Insight"
        root.addView(refreshBtn)

        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        summaryCard.addView(
            UiKit.cardTitle(
                context,
                "SYSTEM STATE"
            )
        )

        val summaryBody = UiKit.bodyText(context, "")
        summaryCard.addView(summaryBody)

        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val primaryCard = UiKit.card(context)
        primaryCard.addView(
            UiKit.cardTitle(
                context,
                "PRIMARY INSIGHT"
            )
        )

        val primaryBody = UiKit.bodyText(context, "")
        primaryCard.addView(primaryBody)

        root.addView(primaryCard)
        root.addView(UiKit.spacer(context))

        val observationCard = UiKit.card(context)
        observationCard.addView(
            UiKit.cardTitle(
                context,
                "OBSERVATIONS"
            )
        )

        val observationBody = UiKit.bodyText(context, "")
        observationCard.addView(observationBody)

        root.addView(observationCard)
        root.addView(UiKit.spacer(context))

        val possibilityCard = UiKit.card(context)
        possibilityCard.addView(
            UiKit.cardTitle(
                context,
                "POSSIBLE DIRECTIONS"
            )
        )

        val possibilityBody = UiKit.bodyText(context, "")
        possibilityCard.addView(possibilityBody)

        root.addView(possibilityCard)

        fun refresh() {

            val profile =
                MachineProfileStore.currentProfile()
                    ?: MachineProfileStore.defaultProfile()

            val machineState =
                MachineStateEngine.currentState()

            val energy =
                EnergyEngine.currentSnapshot()

            val stability =
                RoastStabilityEngine.lastResult()

            val report =
                RoastInsightEngine.analyze(
                    profile = profile,
                    machineState = machineState,
                    energy = energy,
                    stability = stability,
                    styleGoal = null
                )

            summaryBody.text =
                report.quietSummary

            val primary =
                RoastInsightEngine.primaryInsight(report)

            primaryBody.text =
                primary?.summary()
                    ?: "System calm."

            val obsText =
                if (report.observations.isEmpty()) {
                    "-"
                } else {
                    report.observations.joinToString(
                        "\n\n"
                    ) {
                        it.summary()
                    }
                }

            observationBody.text =
                obsText

            val posText =
                if (report.possibilities.isEmpty()) {
                    "-"
                } else {
                    report.possibilities.joinToString(
                        "\n\n"
                    ) {
                        it.summary()
                    }
                }

            possibilityBody.text =
                posText
        }

        refreshBtn.setOnClickListener {
            refresh()
        }

        refresh()

        scroll.addView(root)
        container.addView(scroll)
    }
}
