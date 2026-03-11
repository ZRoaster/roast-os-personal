package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastKnowledgeEngine
import com.roastos.app.RoastKnowledgeRecord
import com.roastos.app.UiKit

object RoastKnowledgePage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST KNOWLEDGE"))
        root.addView(UiKit.pageSubtitle(context, "Exploration and learning records"))
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK TO STUDIO")
        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "SUMMARY"))
        summaryCard.addView(
            UiKit.bodyText(
                context,
                RoastKnowledgeEngine.summary()
            )
        )

        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val records = RoastKnowledgeEngine.buildKnowledge()

        if (records.isEmpty()) {
            val emptyCard = UiKit.card(context)
            emptyCard.addView(UiKit.cardTitle(context, "NO KNOWLEDGE"))
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No knowledge records yet."
                )
            )
            root.addView(emptyCard)
        } else {
            records.forEachIndexed { index, record ->
                val card = UiKit.card(context)

                card.addView(
                    UiKit.cardTitle(
                        context,
                        "RECORD ${index + 1}"
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
        record: RoastKnowledgeRecord
    ): String {
        return """
Batch
${record.batchId}

Exploration
${record.explorationStatus}

Roast Health
${record.roastHealth}

Risk Events
${record.riskEvents}

Cup Score
${record.cupScore ?: "-"}

Bean Color
${record.beanColor ?: "-"}

Aw
${record.aw ?: "-"}

Result
${record.resultTag}
        """.trimIndent()
    }
}
