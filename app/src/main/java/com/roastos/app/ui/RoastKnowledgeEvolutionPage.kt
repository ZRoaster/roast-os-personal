package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastKnowledgeEvolutionEngine
import com.roastos.app.RoastKnowledgeEvolutionRecord
import com.roastos.app.UiKit

object RoastKnowledgeEvolutionPage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "KNOWLEDGE EVOLUTION"))
        root.addView(UiKit.pageSubtitle(context, "Knowledge evolution and promotion candidates"))
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK TO STUDIO")
        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "SUMMARY"))
        summaryCard.addView(
            UiKit.bodyText(
                context,
                RoastKnowledgeEvolutionEngine.summary()
            )
        )

        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val records: List<RoastKnowledgeEvolutionRecord> =
            RoastKnowledgeEvolutionEngine.buildRecords()

        if (records.isEmpty()) {
            val emptyCard = UiKit.card(context)
            emptyCard.addView(UiKit.cardTitle(context, "NO EVOLUTION"))
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No knowledge evolution records yet."
                )
            )
            root.addView(emptyCard)
        } else {
            for (i in records.indices) {
                val record = records[i]

                val card = UiKit.card(context)
                card.addView(
                    UiKit.cardTitle(
                        context,
                        "EVOLUTION ${i + 1}"
                    )
                )
                card.addView(
                    UiKit.bodyText(
                        context,
                        buildRecordText(record)
                    )
                )

                root.addView(card)

                if (i != records.lastIndex) {
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
        record: RoastKnowledgeEvolutionRecord
    ): String {
        return """
Batch
${record.batchId}

Source Tag
${record.sourceTag}

Evolved State
${record.evolvedState}

Confidence
${record.confidence}

Recommendation
${record.recommendation}
        """.trimIndent()
    }
}
