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
        root.addView(UiKit.pageSubtitle(context, "Latest roast history"))
        root.addView(UiKit.spacer(context))

        val topCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "BACK TO STUDIO")

        topCard.addView(UiKit.cardTitle(context, "NAVIGATION"))
        topCard.addView(backBtn)

        root.addView(topCard)
        root.addView(UiKit.spacer(context))

        val entries = RoastHistoryEngine.all()

        if (entries.isEmpty()) {
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
            entries.forEachIndexed { index, entry ->

                val itemCard = UiKit.card(context)
                val itemTitle = UiKit.cardTitle(context, "ROAST ${index + 1}")
                val itemBody = UiKit.bodyText(context, buildCompactEntryText(entry))
                val openBtn = UiKit.secondaryButton(context, "OPEN DETAIL")

                openBtn.setOnClickListener {
                    HistoryDetailPage.show(
                        context = context,
                        container = container,
                        entry = entry
                    )
                }

                itemCard.addView(itemTitle)
                itemCard.addView(itemBody)
                itemCard.addView(UiKit.spacer(context))
                itemCard.addView(openBtn)

                root.addView(itemCard)

                if (index != entries.lastIndex) {
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

    private fun buildCompactEntryText(
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
        """.trimIndent()
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
