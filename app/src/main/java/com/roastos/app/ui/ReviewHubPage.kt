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
        root.addView(UiKit.pageSubtitle(context, "Inspect roast results and compare batches"))
        root.addView(UiKit.spacerS(context))
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.REVIEW
            )
        )
        root.addView(UiKit.spacer(context))

        val recentCard = UiKit.card(context)
        val openRecentBtn = UiKit.primaryButton(context, "Recent Roasts")
        recentCard.addView(UiKit.cardTitle(context, "RECENT"))
        recentCard.addView(UiKit.helperText(context, "Browse saved batches first."))
        recentCard.addView(UiKit.spacerM(context))
        recentCard.addView(openRecentBtn)
        root.addView(recentCard)
        root.addView(UiKit.spacer(context))

        val lastResultCard = UiKit.card(context)
        val openLastDetailBtn = UiKit.secondaryButton(context, "Open Last Roast")
        val lastResultBody = UiKit.bodyText(context, "")
        lastResultCard.addView(UiKit.cardTitle(context, "LATEST RESULT"))
        lastResultCard.addView(UiKit.helperText(context, "A quick summary of the latest saved roast."))
        lastResultCard.addView(UiKit.spacerS(context))
        lastResultCard.addView(lastResultBody)
        lastResultCard.addView(UiKit.spacerM(context))
        lastResultCard.addView(openLastDetailBtn)
        root.addView(lastResultCard)
        root.addView(UiKit.spacer(context))

        val compareCard = UiKit.card(context)
        val openLastCompareBtn = UiKit.secondaryButton(context, "Open Last Compare")
        val compareBody = UiKit.bodyText(context, "")
        compareCard.addView(UiKit.cardTitle(context, "LATEST COMPARE"))
        compareCard.addView(UiKit.helperText(context, "Compare the most recent two saved roasts."))
        compareCard.addView(UiKit.spacerS(context))
        compareCard.addView(compareBody)
        compareCard.addView(UiKit.spacerM(context))
        compareCard.addView(openLastCompareBtn)
        root.addView(compareCard)
        root.addView(UiKit.spacer(context))

        val accessCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "Home")
        accessCard.addView(UiKit.cardTitle(context, "MORE ACCESS"))
        accessCard.addView(UiKit.helperText(context, "Return to the app home when needed."))
        accessCard.addView(UiKit.spacerM(context))
        accessCard.addView(backBtn)
        root.addView(accessCard)

        fun render() {
            val allEntries = RoastHistoryEngine.all()
            val latest = RoastHistoryEngine.latest()

            lastResultBody.text = if (latest == null) {
                """
暂无历史记录

保存第一锅后可进入单锅复盘
                """.trimIndent()
            } else {
                """
批次
${latest.batchId}

结果 / 健康
${latest.batchStatus} / ${latest.roastHealthHeadline}

评测
${if (latest.evaluation != null) "已保存" else "未保存"}
                """.trimIndent()
            }

            compareBody.text = if (allEntries.size < 2) {
                """
暂无可对比批次

至少需要 2 锅历史记录
                """.trimIndent()
            } else {
                val latestEntry = allEntries[0]
                val previous = allEntries[1]

                """
对比对象
${previous.batchId} → ${latestEntry.batchId}

建议
先查看最近两锅差异
                """.trimIndent()
            }

            openLastDetailBtn.isEnabled = latest != null
            openLastCompareBtn.isEnabled = allEntries.size >= 2
        }

        openRecentBtn.setOnClickListener {
            RecentRoastListPage.show(
                context = context,
                container = container,
                onBack = { show(context, container, onBack) }
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
                onBack = { show(context, container, onBack) }
            )
        }

        openLastCompareBtn.setOnClickListener {
            val allEntries = RoastHistoryEngine.all()
            if (allEntries.size < 2) {
                Toast.makeText(context, "Need at least 2 roast history entries", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val latest = allEntries[0]
            val previous = allEntries[1]

            RoastComparePage.show(
                context = context,
                container = container,
                left = previous,
                right = latest,
                onBack = { show(context, container, onBack) }
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
