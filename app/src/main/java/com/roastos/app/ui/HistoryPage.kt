package com.roastos.app.ui

import android.content.Context
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastHistoryEntry

object HistoryPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST HISTORY"))
        root.addView(UiKit.pageSubtitle(context, "View roast history, open batch detail, delete single entries, or clear all history"))
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        actionCard.addView(UiKit.cardTitle(context, "HISTORY ACTIONS"))

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh History"

        val clearBtn = Button(context)
        clearBtn.text = "Clear All History"

        val summaryBody = UiKit.bodyText(context, RoastHistoryEngine.summary())

        actionCard.addView(refreshBtn)
        actionCard.addView(clearBtn)
        actionCard.addView(summaryBody)

        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val listHost = LinearLayout(context)
        listHost.orientation = LinearLayout.VERTICAL
        root.addView(listHost)

        fun renderList() {
            summaryBody.text = RoastHistoryEngine.summary()
            listHost.removeAllViews()

            val items = RoastHistoryEngine.all()

            if (items.isEmpty()) {
                listHost.addView(
                    UiKit.buildCard(
                        context,
                        "NO HISTORY",
                        "No roast history saved yet."
                    )
                )
                return
            }

            items.forEachIndexed { index, entry ->
                listHost.addView(buildEntryCard(context, container, entry, ::renderList))
                if (index != items.lastIndex) {
                    listHost.addView(UiKit.spacer(context))
                }
            }
        }

        refreshBtn.setOnClickListener {
            renderList()
        }

        clearBtn.setOnClickListener {
            RoastHistoryEngine.clear()
            renderList()
        }

        renderList()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildEntryCard(
        context: Context,
        container: LinearLayout,
        entry: RoastHistoryEntry,
        onChanged: () -> Unit
    ): LinearLayout {
        val card = UiKit.card(context)

        val title = TextView(context)
        title.text = entry.batchId
        title.textSize = 18f
        title.setTypeface(null, Typeface.BOLD)

        val body = TextView(context)
        body.textSize = 15f
        body.text = buildEntryBody(entry)
        body.setPadding(0, UiKit.INNER_GAP, 0, 0)

        val openDetailBtn = Button(context)
        openDetailBtn.text = "Open Detail"

        val deleteBtn = Button(context)
        deleteBtn.text = "Delete This Record"

        openDetailBtn.setOnClickListener {
            BatchDetailPage.show(
                context = context,
                container = container,
                batchId = entry.batchId
            )
        }

        deleteBtn.setOnClickListener {
            RoastHistoryEngine.delete(entry.batchId)
            onChanged()
        }

        card.addView(title)
        card.addView(body)
        card.addView(openDetailBtn)
        card.addView(deleteBtn)

        return card
    }

    private fun buildEntryBody(entry: RoastHistoryEntry): String {
        val predicted = """
Predicted
Turning ${entry.predictedTurningSec?.toString() ?: "-"}
Yellow  ${entry.predictedYellowSec?.toString() ?: "-"}
FC      ${entry.predictedFcSec?.toString() ?: "-"}
Drop    ${entry.predictedDropSec?.toString() ?: "-"}
        """.trimIndent()

        val actual = """
Actual
Turning ${entry.actualTurningSec?.toString() ?: "-"}
Yellow  ${entry.actualYellowSec?.toString() ?: "-"}
FC      ${entry.actualFcSec?.toString() ?: "-"}
Drop    ${entry.actualDropSec?.toString() ?: "-"}
ROR     ${entry.actualPreFcRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()

        return """
Status
${entry.batchStatus}

Bean
${entry.process.ifBlank { "-" }}
Density  ${"%.1f".format(entry.density)}
Moisture ${"%.1f".format(entry.moisture)}
aw       ${"%.2f".format(entry.aw)}

Environment
Temp ${"%.1f".format(entry.envTemp)}℃
RH   ${"%.1f".format(entry.envRh)}%

$predicted

$actual
        """.trimIndent()
    }
}
