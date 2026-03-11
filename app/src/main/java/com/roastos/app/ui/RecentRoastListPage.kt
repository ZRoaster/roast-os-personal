package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastHistoryEntry
import com.roastos.app.UiKit

object RecentRoastListPage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "RECENT ROASTS"))
        root.addView(UiKit.pageSubtitle(context, "Recent roast history list"))
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK TO STUDIO")
        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        val all = RoastHistoryEngine.all()

        if (all.isEmpty()) {
            val emptyCard = UiKit.card(context)
            emptyCard.addView(UiKit.cardTitle(context, "NO HISTORY"))
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No roast history yet."
                )
            )
            root.addView(emptyCard)
        } else {
            all.forEachIndexed { index, entry ->

                val card = UiKit.card(context)

                card.addView(
                    UiKit.cardTitle(
                        context,
                        "ROAST ${index + 1}"
                    )
                )

                card.addView(
                    UiKit.bodyText(
                        context,
                        buildEntryText(entry)
                    )
                )

                val openBtn = UiKit.secondaryButton(context, "OPEN DETAIL")
                openBtn.setOnClickListener {
                    HistoryDetailPage.show(
                        context = context,
                        container = container,
                        entry = entry
                    )
                }

                card.addView(UiKit.spacer(context))
                card.addView(openBtn)

                root.addView(card)
                root.addView(UiKit.spacer(context))
            }
        }

        backBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildEntryText(
        entry: RoastHistoryEntry
    ): String {
        return """
Batch
${entry.batchId}

Status
${entry.batchStatus}

Health
${entry.roastHealthHeadline}

Time
${formatTime(entry.createdAtMillis)}

Report
${entry.reportText.take(160).ifBlank { "-" }}
        """.trimIndent()
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
