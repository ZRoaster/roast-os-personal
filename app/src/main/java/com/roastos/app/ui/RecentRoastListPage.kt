package com.roastos.app.ui

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastHistoryEntry
import com.roastos.app.UiKit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RecentRoastListPage {

    private const val FILTER_ALL = "ALL"
    private const val FILTER_IDLE = "IDLE"
    private const val FILTER_RUNNING = "RUNNING"
    private const val FILTER_STOPPED = "STOPPED"
    private const val FILTER_FINISHED = "FINISHED"
    private const val FILTER_ONLY_EVALUATED = "ONLY EVALUATED"

    private var selectedBatchA: String? = null
    private var selectedBatchB: String? = null

    fun show(
        context: Context,
        container: LinearLayout,
        onBack: (() -> Unit)? = null
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "RECENT ROASTS"))
        root.addView(UiKit.pageSubtitle(context, "Latest roast history"))
        root.addView(UiKit.spacer(context))

        val topCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "BACK")
        val clearAllBtn = UiKit.secondaryButton(context, "CLEAR ALL HISTORY")

        topCard.addView(UiKit.cardTitle(context, "NAVIGATION"))
        topCard.addView(backBtn)
        topCard.addView(clearAllBtn)

        root.addView(topCard)
        root.addView(UiKit.spacer(context))

        val compareCard = UiKit.card(context)
        val compareStateText = UiKit.bodyText(context, "")
        val clearCompareBtn = UiKit.secondaryButton(context, "CLEAR COMPARE")
        val openCompareBtn = UiKit.primaryButton(context, "OPEN COMPARE")

        compareCard.addView(UiKit.cardTitle(context, "COMPARE"))
        compareCard.addView(compareStateText)
        compareCard.addView(UiKit.spacer(context))
        compareCard.addView(clearCompareBtn)
        compareCard.addView(openCompareBtn)

        root.addView(compareCard)
        root.addView(UiKit.spacer(context))

        val filterCard = UiKit.card(context)
        val searchInput = EditText(context).apply {
            hint = "Search batch / title / process"
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
        }

        val resultCountText = UiKit.bodyText(context, "")

        val filterButtonRow1 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val filterButtonRow2 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val allBtn = UiKit.secondaryButton(context, FILTER_ALL)
        val idleBtn = UiKit.secondaryButton(context, FILTER_IDLE)
        val runningBtn = UiKit.secondaryButton(context, FILTER_RUNNING)
        val stoppedBtn = UiKit.secondaryButton(context, FILTER_STOPPED)
        val finishedBtn = UiKit.secondaryButton(context, FILTER_FINISHED)
        val onlyEvaluatedBtn = UiKit.secondaryButton(context, FILTER_ONLY_EVALUATED)

        filterButtonRow1.addView(allBtn)
        filterButtonRow1.addView(idleBtn)
        filterButtonRow1.addView(runningBtn)

        filterButtonRow2.addView(stoppedBtn)
        filterButtonRow2.addView(finishedBtn)
        filterButtonRow2.addView(onlyEvaluatedBtn)

        filterCard.addView(UiKit.cardTitle(context, "FILTER"))
        filterCard.addView(searchInput)
        filterCard.addView(UiKit.spacer(context))
        filterCard.addView(filterButtonRow1)
        filterCard.addView(filterButtonRow2)
        filterCard.addView(UiKit.spacer(context))
        filterCard.addView(resultCountText)

        root.addView(filterCard)
        root.addView(UiKit.spacer(context))

        val listHost = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(listHost)

        val allEntries = RoastHistoryEngine.all()
        var selectedFilter = FILTER_ALL

        fun selectedEntryA(): RoastHistoryEntry? {
            return selectedBatchA?.let { RoastHistoryEngine.findByBatchId(it) }
        }

        fun selectedEntryB(): RoastHistoryEntry? {
            return selectedBatchB?.let { RoastHistoryEngine.findByBatchId(it) }
        }

        fun evaluationStatusText(entry: RoastHistoryEntry?): String {
            return if (entry?.evaluation != null) "Saved" else "Not saved"
        }

        fun refreshCompareState() {
            val a = selectedEntryA()
            val b = selectedEntryB()

            compareStateText.text = """
A
${a?.batchId ?: "-"}
Evaluation
${evaluationStatusText(a)}

B
${b?.batchId ?: "-"}
Evaluation
${evaluationStatusText(b)}
            """.trimIndent()

            openCompareBtn.isEnabled = a != null && b != null && a.batchId != b.batchId
        }

        fun applyFilter() {
            val keyword = searchInput.text?.toString()?.trim().orEmpty()

            val filtered = allEntries.filter { entry ->
                matchesKeyword(entry, keyword) &&
                    matchesStatus(entry, selectedFilter) &&
                    matchesEvaluation(entry, selectedFilter)
            }

            resultCountText.text = "Showing ${filtered.size} / ${allEntries.size}"

            listHost.removeAllViews()

            if (filtered.isEmpty()) {
                val emptyCard = UiKit.card(context)
                emptyCard.addView(UiKit.cardTitle(context, "NO MATCH"))
                emptyCard.addView(
                    UiKit.bodyText(
                        context,
                        if (allEntries.isEmpty()) {
                            "No roast history yet."
                        } else {
                            "No roast history matched current search/filter."
                        }
                    )
                )
                listHost.addView(emptyCard)
                refreshCompareState()
                return
            }

            filtered.forEachIndexed { index, entry ->
                val itemCard = UiKit.card(context)
                val itemTitle = UiKit.cardTitle(context, "ROAST ${index + 1}")
                val itemBody = UiKit.bodyText(context, buildCompactEntryText(entry))
                val selectABtn = UiKit.secondaryButton(
                    context,
                    if (selectedBatchA == entry.batchId) "SELECTED A" else "SELECT A"
                )
                val selectBBtn = UiKit.secondaryButton(
                    context,
                    if (selectedBatchB == entry.batchId) "SELECTED B" else "SELECT B"
                )
                val openBtn = UiKit.secondaryButton(context, "OPEN DETAIL")

                selectABtn.setOnClickListener {
                    selectedBatchA = entry.batchId
                    if (selectedBatchB == entry.batchId) {
                        selectedBatchB = null
                    }
                    show(
                        context = context,
                        container = container,
                        onBack = onBack
                    )
                }

                selectBBtn.setOnClickListener {
                    selectedBatchB = entry.batchId
                    if (selectedBatchA == entry.batchId) {
                        selectedBatchA = null
                    }
                    show(
                        context = context,
                        container = container,
                        onBack = onBack
                    )
                }

                openBtn.setOnClickListener {
                    HistoryDetailPage.show(
                        context = context,
                        container = container,
                        entry = entry,
                        onBack = {
                            show(
                                context = context,
                                container = container,
                                onBack = onBack
                            )
                        }
                    )
                }

                itemCard.addView(itemTitle)
                itemCard.addView(itemBody)
                itemCard.addView(UiKit.spacer(context))
                itemCard.addView(selectABtn)
                itemCard.addView(selectBBtn)
                itemCard.addView(openBtn)

                listHost.addView(itemCard)

                if (index != filtered.lastIndex) {
                    listHost.addView(UiKit.spacer(context))
                }
            }

            refreshCompareState()
        }

        fun setFilter(filter: String) {
            selectedFilter = filter
            applyFilter()
        }

        allBtn.setOnClickListener { setFilter(FILTER_ALL) }
        idleBtn.setOnClickListener { setFilter(FILTER_IDLE) }
        runningBtn.setOnClickListener { setFilter(FILTER_RUNNING) }
        stoppedBtn.setOnClickListener { setFilter(FILTER_STOPPED) }
        finishedBtn.setOnClickListener { setFilter(FILTER_FINISHED) }
        onlyEvaluatedBtn.setOnClickListener { setFilter(FILTER_ONLY_EVALUATED) }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                applyFilter()
            }
        })

        backBtn.setOnClickListener {
            onBack?.invoke() ?: RoastStudioPage.show(context, container)
        }

        clearAllBtn.setOnClickListener {
            if (RoastHistoryEngine.count() == 0) {
                Toast.makeText(context, "No history to clear", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(context)
                .setTitle("Clear all history?")
                .setMessage("This will permanently remove all saved roast history on this device.")
                .setPositiveButton("CLEAR") { _, _ ->
                    val result = RoastHistoryEngine.clear()
                    selectedBatchA = null
                    selectedBatchB = null
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()

                    show(
                        context = context,
                        container = container,
                        onBack = onBack
                    )
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }

        clearCompareBtn.setOnClickListener {
            selectedBatchA = null
            selectedBatchB = null
            show(
                context = context,
                container = container,
                onBack = onBack
            )
        }

        openCompareBtn.setOnClickListener {
            val left = selectedEntryA()
            val right = selectedEntryB()

            if (left == null || right == null) {
                Toast.makeText(context, "Select A and B first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RoastComparePage.show(
                context = context,
                container = container,
                left = left,
                right = right,
                onBack = {
                    show(
                        context = context,
                        container = container,
                        onBack = onBack
                    )
                }
            )
        }

        refreshCompareState()
        applyFilter()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun matchesKeyword(
        entry: RoastHistoryEntry,
        keyword: String
    ): Boolean {
        if (keyword.isBlank()) return true

        val q = keyword.lowercase(Locale.getDefault())

        return entry.batchId.lowercase(Locale.getDefault()).contains(q) ||
            entry.title.lowercase(Locale.getDefault()).contains(q) ||
            entry.process.lowercase(Locale.getDefault()).contains(q)
    }

    private fun matchesStatus(
        entry: RoastHistoryEntry,
        selectedFilter: String
    ): Boolean {
        if (selectedFilter == FILTER_ALL || selectedFilter == FILTER_ONLY_EVALUATED) return true
        return entry.batchStatus.trim().equals(selectedFilter, ignoreCase = true)
    }

    private fun matchesEvaluation(
        entry: RoastHistoryEntry,
        selectedFilter: String
    ): Boolean {
        if (selectedFilter != FILTER_ONLY_EVALUATED) return true
        return entry.evaluation != null
    }

    private fun buildCompactEntryText(
        entry: RoastHistoryEntry
    ): String {
        return """
Batch
${entry.batchId}

Title
${entry.title}

Process
${entry.process}

Status
${entry.batchStatus}

Health
${entry.roastHealthHeadline}

Evaluation
${if (entry.evaluation != null) "Saved" else "Not saved"}

Created
${formatDateTime(entry.createdAtMillis)}
        """.trimIndent()
    }

    private fun formatDateTime(ms: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(ms))
    }
}
