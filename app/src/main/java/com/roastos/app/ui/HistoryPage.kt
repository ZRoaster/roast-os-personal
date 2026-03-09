package com.roastos.app.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastHistoryEntry

object HistoryPage {

    private var filterEvaluatedOnly = false
    private var filterHighRiskOnly = false
    private var searchQuery = ""

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST HISTORY"))
        root.addView(UiKit.pageSubtitle(context, "Search / Filter roast batches"))
        root.addView(UiKit.spacer(context))

        val actionCard = UiKit.card(context)
        actionCard.addView(UiKit.cardTitle(context, "SEARCH & FILTER"))

        val searchInput = EditText(context)
        searchInput.hint = "Search BatchId / Process / Headline"

        val evaluatedBtn = Button(context)
        val riskBtn = Button(context)
        val resetBtn = Button(context)
        val refreshBtn = Button(context)
        val clearBtn = Button(context)

        val summaryText = UiKit.bodyText(context, "")

        actionCard.addView(searchInput)
        actionCard.addView(evaluatedBtn)
        actionCard.addView(riskBtn)
        actionCard.addView(resetBtn)
        actionCard.addView(refreshBtn)
        actionCard.addView(clearBtn)
        actionCard.addView(summaryText)

        root.addView(actionCard)
        root.addView(UiKit.spacer(context))

        val listHost = LinearLayout(context)
        listHost.orientation = LinearLayout.VERTICAL
        root.addView(listHost)

        fun updateButtons() {
            evaluatedBtn.text =
                if (filterEvaluatedOnly) "Evaluated Only ON"
                else "Evaluated Only OFF"

            riskBtn.text =
                if (filterHighRiskOnly) "High Risk Only ON"
                else "High Risk Only OFF"
        }

        fun filteredItems(): List<RoastHistoryEntry> {
            return RoastHistoryEngine.all().filter { entry ->

                val passEval =
                    !filterEvaluatedOnly || entry.evaluation != null

                val passRisk =
                    !filterHighRiskOnly || buildRisk(entry) == "High"

                val passSearch =
                    searchQuery.isBlank() ||
                        entry.batchId.contains(searchQuery, true) ||
                        entry.process.contains(searchQuery, true) ||
                        buildHeadline(entry).contains(searchQuery, true)

                passEval && passRisk && passSearch
            }
        }

        fun render() {
            updateButtons()

            val items = filteredItems()

            summaryText.text = """
History Summary

Total Batches
${RoastHistoryEngine.count()}

Filtered Results
${items.size}

Search
${if (searchQuery.isBlank()) "None" else searchQuery}

Filters
Evaluated ${if (filterEvaluatedOnly) "ON" else "OFF"}
High Risk ${if (filterHighRiskOnly) "ON" else "OFF"}
""".trimIndent()

            listHost.removeAllViews()

            if (items.isEmpty()) {
                listHost.addView(
                    UiKit.buildCard(
                        context,
                        "NO MATCH",
                        "No batches match the current filters"
                    )
                )
                return
            }

            items.forEachIndexed { index, entry ->

                listHost.addView(
                    buildEntryCard(context, container, entry, ::render)
                )

                if (index != items.lastIndex) {
                    listHost.addView(UiKit.spacer(context))
                }
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString()
                render()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })

        evaluatedBtn.setOnClickListener {
            filterEvaluatedOnly = !filterEvaluatedOnly
            render()
        }

        riskBtn.setOnClickListener {
            filterHighRiskOnly = !filterHighRiskOnly
            render()
        }

        resetBtn.setOnClickListener {
            filterEvaluatedOnly = false
            filterHighRiskOnly = false
            searchQuery = ""
            searchInput.setText("")
            render()
        }

        refreshBtn.setOnClickListener {
            render()
        }

        clearBtn.setOnClickListener {
            RoastHistoryEngine.clear()
            render()
        }

        render()

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
        body.text = buildEntryBody(entry)
        body.setPadding(0, UiKit.INNER_GAP, 0, 0)

        val openBtn = Button(context)
        openBtn.text = "Open Detail"

        val deleteBtn = Button(context)
        deleteBtn.text = "Delete Record"

        openBtn.setOnClickListener {
            BatchDetailPage.show(context, container, entry.batchId)
        }

        deleteBtn.setOnClickListener {
            RoastHistoryEngine.delete(entry.batchId)
            onChanged()
        }

        card.addView(title)
        card.addView(body)
        card.addView(openBtn)
        card.addView(deleteBtn)

        return card
    }

    private fun buildEntryBody(entry: RoastHistoryEntry): String {

        val headline = buildHeadline(entry)
        val replay = buildReplayability(entry)
        val risk = buildRisk(entry)
        val evaluation = if (entry.evaluation != null) "Saved" else "Not saved"

        val baselineTrace = """
Baseline Trace
Source ${entry.baselineSource ?: "-"}
Label  ${entry.baselineLabel ?: "-"}
Match  ${formatBaselineMatch(entry.baselineMatchGrade)}
        """.trimIndent()

        return """
Headline
$headline

Replayability
$replay

Risk
$risk

Evaluation
$evaluation

$baselineTrace

Bean
${entry.process.ifBlank { "-" }}

Density ${"%.1f".format(entry.density)}
Moisture ${"%.1f".format(entry.moisture)}
aw ${"%.2f".format(entry.aw)}

Environment
Temp ${"%.1f".format(entry.envTemp)}℃
RH ${"%.1f".format(entry.envRh)}%
        """.trimIndent()
    }

    private fun buildHeadline(entry: RoastHistoryEntry): String {

        val ror = entry.actualPreFcRor

        return when {
            ror != null && ror >= 10.8 -> "Late stage acceleration"
            ror != null && ror <= 7.0 -> "Energy collapse risk"
            else -> "Close to plan"
        }
    }

    private fun buildReplayability(entry: RoastHistoryEntry): String {

        val ror = entry.actualPreFcRor ?: return "Medium"

        return when {
            ror in 8.0..9.5 -> "High"
            ror in 7.0..10.8 -> "Medium"
            else -> "Low"
        }
    }

    private fun buildRisk(entry: RoastHistoryEntry): String {

        val ror = entry.actualPreFcRor ?: return "Minor"

        return when {
            ror >= 10.8 || ror <= 7.0 -> "High"
            ror >= 9.5 || ror <= 8.0 -> "Medium"
            else -> "Low"
        }
    }

    private fun formatBaselineMatch(raw: String?): String {
        return when (raw) {
            "EXACT_MATCH" -> "Exact Match"
            "SIMILAR_MATCH" -> "Similar Match"
            "REFERENCE_ONLY" -> "Reference Only"
            else -> "-"
        }
    }
}
