package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastExperimentRecord
import com.roastos.app.RoastExperimentTracker
import com.roastos.app.UiKit

object RoastExperimentPage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST EXPERIMENTS"))
        root.addView(UiKit.pageSubtitle(context, "Experiment tracking and validation"))
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK TO STUDIO")
        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "SUMMARY"))
        summaryCard.addView(
            UiKit.bodyText(
                context,
                RoastExperimentTracker.summary()
            )
        )

        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val records = RoastExperimentTracker.buildRecords()

        if (records.isEmpty()) {
            val emptyCard = UiKit.card(context)
            emptyCard.addView(UiKit.cardTitle(context, "NO EXPERIMENTS"))
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No experiment records yet."
                )
            )
            root.addView(emptyCard)
        } else {
            records.forEachIndexed { index, record ->
                val card = UiKit.card(context)

                card.addView(
                    UiKit.cardTitle(
                        context,
                        "EXPERIMENT ${index + 1}"
                    )
                )

                card.addView(
                    UiKit.bodyText(
                        context,
                        buildRecordText(record)
                    )
                )

                root.addView(card)

                if (index != records.lastIndex) {
                    root.addView(UiKit.spacer(context))
                }
            }
        }

        backBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildRecordText(
        record: RoastExperimentRecord
    ): String {
        return """
Batch
${record.batchId}

Risk Events
${record.riskEventCount}

Cup Result
${if (record.hasCupResult) "Yes" else "No"}

Cup Score
${record.cupScore ?: "-"}

Bean Color
${record.beanColor ?: "-"}

Aw
${record.aw ?: "-"}

Status
${record.experimentStatus}

Recommendation
${record.recommendation}
        """.trimIndent()
    }
}
