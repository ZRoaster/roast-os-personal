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
    private const val FILTER_ONLY_NOT_EVALUATED = "ONLY NOT EVALUATED"

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
        root.addView(UiKit.pageSubtitle(context, "Browse batches, select one, or compare two"))
        root.addView(UiKit.spacerS(context))
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.REVIEW
            )
        )
        root.addView(UiKit.spacer(context))

        val accessCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "Back")
        val clearAllBtn = UiKit.secondaryButton(context, "Clear All History")
        accessCard.addView(UiKit.cardTitle(context, "ACCESS"))
        accessCard.addView(UiKit.helperText(context, "Return to review or manage local history."))
        accessCard.addView(UiKit.spacerM(context))
        accessCard.addView(backBtn)
        accessCard.addView(UiKit.spacerS(context))
        accessCard.addView(clearAllBtn)
        root.addView(accessCard)
        root.addView(UiKit.spacer(context))

        val compareCard = UiKit.card(context)
        val compareStateText = UiKit.bodyText(context, "")
        val clearCompareBtn = UiKit.secondaryButton(context, "Clear Compare")
        val openCompareBtn = UiKit.primaryButton(context, "Open Compare")

        compareCard.addView(UiKit.cardTitle(context, "COMPARE"))
        compareCard.addView(UiKit.helperText(context, "Select A and B from the list below."))
        compareCard.addView(UiKit.spacerS(context))
        compareCard.addView(compareStateText)
        compareCard.addView(UiKit.spacerM(context))
        compareCard.addView(openCompareBtn)
        compareCard.addView(UiKit.spacerS(context))
        compareCard.addView(clearCompareBtn)
        root.addView(compareCard)
        root.addView(UiKit.spacer(context))

        val filterCard = UiKit.card(context)
        val searchInput = EditText(context).apply {
            hint = "Search batch / title / process"
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
        }

        val resultCountText = UiKit.helperText(context, "")

        val filterButtonRow1 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val filterButtonRow2 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val filterButtonRow3 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val allBtn = UiKit.secondaryButton(context, "All")
        val idleBtn = UiKit.secondaryButton(context, "Idle")
        val runningBtn = UiKit.secondaryButton(context, "Running")
        val stoppedBtn = UiKit.secondaryButton(context, "Stopped")
        val finishedBtn = UiKit.secondaryButton(context, "Finished")
        val onlyEvaluatedBtn = UiKit.secondaryButton(context, "Evaluated")
        val onlyNotEvaluatedBtn = UiKit.secondaryButton(context, "Not Evaluated")

        filterButtonRow1.addView(allBtn)
        filterButtonRow1.addView(idleBtn)
        filterButtonRow1.addView(runningBtn)

        filterButtonRow2.addView(stoppedBtn)
        filterButtonRow2.addView(finishedBtn)

        filterButtonRow3.addView(onlyEvaluatedBtn)
        filterButtonRow3.addView(onlyNotEvaluatedBtn)

        filterCard.addView(UiKit.cardTitle(context, "FILTER"))
        filterCard.addView(UiKit.helperText(context, "Search or narrow the roast list."))
        filterCard.addView(UiKit.spacerS(context))
        filterCard.addView(searchInput)
        filterCard.addView(UiKit.spacerM(context))
        filterCard.addView(filterButtonRow1)
        filterCard.addView(UiKit.spacerS(context))
        filterCard.addView(filterButtonRow2)
        filterCard.addView(UiKit.spacerS(context))
        filterCard.addView(filterButtonRow3)
        filterCard.addView(UiKit.spacerM(context))
        filterCard.addView(resultCountText)

        root.addView(filterCard)
        root.addView(UiKit.spacer(context))

        val listSectionTitle = UiKit.card(context)
        listSectionTitle.addView(UiKit.cardTitle(context, "ROAST LIST"))
        listSectionTitle.addView(UiKit.helperText(context, "Open one roast or mark two for comparison."))
        root.addView(listSectionTitle)
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

        fun buildCompareSlot(entry: RoastHistoryEntry?): String {
            if (entry == null) {
                return """
批次
-

结果 / 评测
- / -

FC / Drop
- / -
                """.trimIndent()
            }

            val fc = formatSec(entry.actualFcSec ?: entry.predictedFcSec)
            val drop = formatSec(entry.actualDropSec ?: entry.predictedDropSec)

            return """
批次
${entry.batchId}

结果 / 评测
${entry.roastHealthHeadline} / ${if (entry.evaluation != null) "已保存" else "未保存"}

FC / Drop
$fc / $drop
            """.trimIndent()
        }

        fun refreshCompareState() {
            val a = selectedEntryA()
            val b = selectedEntryB()

            compareStateText.text = """
A
${buildCompareSlot(a)}

B
${buildCompareSlot(b)}
            """.trimIndent()

            openCompareBtn.isEnabled = a != null && b != null && a.batchId != b.batchId
        }

        fun buildRoastRow(entry: RoastHistoryEntry): LinearLayout {
            val rowCard = UiKit.card(context)

            val title = UiKit.cardTitle(context, entry.batchId)
            val subtitle = UiKit.helperText(
                context,
                "${formatDateTime(entry.createdAtMillis)} · ${entry.batchStatus}"
            )
            val summary = UiKit.bodyText(
                context,
                """
健康 / 评测
${entry.roastHealthHeadline} / ${if (entry.evaluation != null) "已保存" else "未保存"}

FC / Drop
${formatSec(entry.actualFcSec ?: entry.predictedFcSec)} / ${formatSec(entry.actualDropSec ?: entry.predictedDropSec)}
                """.trimIndent()
            )

            val actionRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val selectABtn = UiKit.secondaryButton(
                context,
                if (selectedBatchA == entry.batchId) "Selected A" else "Select A"
            )
            val selectBBtn = UiKit.secondaryButton(
                context,
                if (selectedBatchB == entry.batchId) "Selected B" else "Select B"
            )
            val openBtn = UiKit.primaryButton(context, "Open")

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

            actionRow.addView(selectABtn)
            actionRow.addView(selectBBtn)

            rowCard.addView(title)
            rowCard.addView(UiKit.spacerS(context))
            rowCard.addView(subtitle)
            rowCard.addView(UiKit.spacerS(context))
            rowCard.addView(summary)
            rowCard.addView(UiKit.spacerM(context))
            rowCard.addView(actionRow)
            rowCard.addView(UiKit.spacerS(context))
            rowCard.addView(openBtn)

            return rowCard
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
                    UiKit.helperText(
                        context,
                        if (allEntries.isEmpty()) {
                            "No roast history yet."
                        } else {
                            "No roast history matched current search or filter."
                        }
                    )
                )
                listHost.addView(emptyCard)
                refreshCompareState()
                return
            }

            filtered.forEachIndexed { index, entry ->
                listHost.addView(buildRoastRow(entry))
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
        onlyNotEvaluatedBtn.setOnClickListener { setFilter(FILTER_ONLY_NOT_EVALUATED) }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                applyFilter()
            }
        })

        backBtn.setOnClickListener {
            onBack?.invoke() ?: ReviewHubPage.show(context, container)
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
        if (
            selectedFilter == FILTER_ALL ||
            selectedFilter == FILTER_ONLY_EVALUATED ||
            selectedFilter == FILTER_ONLY_NOT_EVALUATED
        ) return true

        return entry.batchStatus.trim().equals(selectedFilter, ignoreCase = true)
    }

    private fun matchesEvaluation(
        entry: RoastHistoryEntry,
        selectedFilter: String
    ): Boolean {
        return when (selectedFilter) {
            FILTER_ONLY_EVALUATED -> entry.evaluation != null
            FILTER_ONLY_NOT_EVALUATED -> entry.evaluation == null
            else -> true
        }
    }

    private fun formatSec(sec: Int?): String {
        if (sec == null) return "-"
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatDateTime(ms: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(ms))
    }
}
