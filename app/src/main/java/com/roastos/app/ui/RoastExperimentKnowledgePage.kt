package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastExperimentKnowledgeEngine
import com.roastos.app.RoastExperimentKnowledgeRecord
import com.roastos.app.UiKit

object RoastExperimentKnowledgePage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "EXPERIMENT KNOWLEDGE"))
        root.addView(UiKit.pageSubtitle(context, "Experiment knowledge and controllable patterns"))
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK TO STUDIO")
        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "SUMMARY"))
        summaryCard.addView(
            UiKit.bodyText(
                context,
                RoastExperimentKnowledgeEngine.summary()
            )
        )

        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val records = RoastExperimentKnowledgeEngine.buildRecords()

        if (records.isEmpty()) {
            val emptyCard = UiKit.card(context)
            emptyCard.addView(UiKit.cardTitle(context, "NO KNOWLEDGE"))
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No experiment knowledge records yet."
                )
            )
            root.addView(emptyCard)
        } else {
            records.forEachIndexed { index, record ->
                val card = UiKit.card(context)

                card.addView(
                    UiKit.cardTitle(
                        context,
                        "KNOWLEDGE ${index + 1}"
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
        record: RoastExperimentKnowledgeRecord
    ): String {
        return """
Batch
${record.batchId}

Risk Events
${record.riskEvents}

Cup Score
${record.cupScore ?: "-"}

Aw
${record.aw ?: "-"}

Bean Color
${record.beanColor ?: "-"}

Knowledge Tag
${record.knowledgeTag}

Recommendation
${record.recommendation}
        """.trimIndent()
    }
}
