package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.UiKit

object ReviewHubPage {

    fun show(
        context: Context,
        container: LinearLayout,
        onBack: (() -> Unit)? = null
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "REVIEW"))
        root.addView(UiKit.pageSubtitle(context, "Inspect past roasts, compare batches, and extract usable experience"))
        root.addView(UiKit.spacer(context))
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.REVIEW
            )
        )
        root.addView(UiKit.spacer(context))

        val recentCard = UiKit.card(context)
        val openRecentBtn = UiKit.primaryButton(context, "OPEN RECENT ROASTS")

        recentCard.addView(UiKit.cardTitle(context, "RECENT ROASTS"))
        recentCard.addView(
            UiKit.helperText(
                context,
                "Browse saved batches first."
            )
        )
        recentCard.addView(UiKit.spacer(context))
        recentCard.addView(openRecentBtn)
        root.addView(recentCard)
        root.addView(UiKit.spacer(context))

        val lastDetailCard = UiKit.card(context)
        val openLastDetailBtn = UiKit.primaryButton(context, "OPEN LAST ROAST DETAIL")
        val lastDetailBody = UiKit.bodyText(context, "")

        lastDetailCard.addView(UiKit.cardTitle(context, "LAST ROAST"))
        lastDetailCard.addView(
            UiKit.helperText(
                context,
                "Jump directly into the latest saved roast."
            )
        )
        lastDetailCard.addView(UiKit.spacer(context))
        lastDetailCard.addView(lastDetailBody)
        lastDetailCard.addView(UiKit.spacer(context))
        lastDetailCard.addView(openLastDetailBtn)
        root.addView(lastDetailCard)
        root.addView(UiKit.spacer(context))

        val compareCard = UiKit.card(context)
        val openLastCompareBtn = UiKit.primaryButton(context, "OPEN LAST COMPARE")
        val compareBody = UiKit.bodyText(context, "")

        compareCard.addView(UiKit.cardTitle(context, "LAST COMPARE"))
        compareCard.addView(
            UiKit.helperText(
                context,
                "Compare the latest two saved roasts."
            )
        )
        compareCard.addView(UiKit.spacer(context))
        compareCard.addView(compareBody)
        compareCard.addView(UiKit.spacer(context))
        compareCard.addView(openLastCompareBtn)
        root.addView(compareCard)
        root.addView(UiKit.spacer(context))

        val navCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "BACK")
        navCard.addView(UiKit.cardTitle(context, "NAVIGATION"))
        navCard.addView(
            UiKit.helperText(
                context,
                "Return to the app home when needed."
            )
        )
        navCard.addView(UiKit.spacer(context))
        navCard.addView(backBtn)
        root.addView(navCard)

        fun render() {
            val allEntries = RoastHistoryEngine.all()
            val latest = RoastHistoryEngine.latest()

            lastDetailBody.text = if (latest == null) {
                """
No roast history yet.

Save your first roast to unlock detail review.
                """.trimIndent()
            } else {
                """
Batch
${latest.batchId}

Status
${latest.batchStatus}

Health
${latest.roastHealthHeadline}

Evaluation
${if (latest.evaluation != null) "Saved" else "Not saved"}
                """.trimIndent()
            }

            compareBody.text = if (allEntries.size < 2) {
                """
Need at least 2 roast history entries.

Save more roasts to unlock direct comparison.
                """.trimIndent()
            } else {
                val latestEntry = allEntries[0]
                val previous = allEntries[1]

                """
A
${previous.batchId}

B
${latestEntry.batchId}
                """.trimIndent()
            }

            openLastDetailBtn.isEnabled = latest != null
            openLastCompareBtn.isEnabled = allEntries.size >= 2
        }

        openRecentBtn.setOnClickListener {
            RecentRoastListPage.show(
                context = context,
                container = container,
                onBack = {
                    show(context, container, onBack)
                }
            )
        }

        openLastDetailBtn.setOnClickListener {
            val latest = RoastHistoryEngine.latest()
            if (latest == null) {
                Toast.makeText(context, "No roast history yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            HistoryDetailPage.show(
                context = context,
                container = container,
                entry = latest,
                onBack = {
                    show(context, container, onBack)
                }
            )
        }

        openLastCompareBtn.setOnClickListener {
            val allEntries = RoastHistoryEngine.all()
            if (allEntries.size < 2) {
                Toast.makeText(
                    context,
                    "Need at least 2 roast history entries",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val latest = allEntries[0]
            val previous = allEntries[1]

            RoastComparePage.show(
                context = context,
                container = container,
                left = previous,
                right = latest,
                onBack = {
                    show(context, container, onBack)
                }
            )
        }

        backBtn.setOnClickListener {
            onBack?.invoke() ?: MainShellPage.show(context, container)
        }

        render()

        scroll.addView(root)
        container.addView(scroll)
    }
}
