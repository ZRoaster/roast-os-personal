package com.roastos.app.ui

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.roastos.app.RoastEvaluation
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastHistoryEntry
import com.roastos.app.RoastStyleFromBatchEngine
import com.roastos.app.UiKit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryDetailPage {

    fun show(
        context: Context,
        container: LinearLayout,
        entry: RoastHistoryEntry?,
        onBack: (() -> Unit)? = null
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST HISTORY DETAIL"))
        root.addView(UiKit.spacer(context))

        val navCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "BACK")

        navCard.addView(UiKit.cardTitle(context, "NAVIGATION"))
        navCard.addView(backBtn)

        root.addView(navCard)
        root.addView(UiKit.spacer(context))

        if (entry == null) {
            val emptyCard = UiKit.card(context)

            emptyCard.addView(UiKit.cardTitle(context, "NO DATA"))
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No roast history entry found."
                )
            )

            root.addView(emptyCard)

            backBtn.setOnClickListener {
                onBack?.invoke() ?: RoastStudioPage.show(context, container)
            }

            scroll.addView(root)
            container.addView(scroll)
            return
        }

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "SUMMARY"))
        summaryCard.addView(
            UiKit.bodyText(
                context,
                buildSummaryStrip(entry)
            )
        )

        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val batchCard = UiKit.card(context)

        batchCard.addView(UiKit.cardTitle(context, "BATCH"))
        batchCard.addView(
            UiKit.bodyText(
                context,
                """
Batch ID
${entry.batchId}

Title
${entry.title}

Created
${formatDateTime(entry.createdAtMillis)}

Status
${entry.batchStatus}

Process
${entry.process}

Density
${entry.density}

Moisture
${entry.moisture}

AW
${entry.aw}

Environment
${entry.envTemp} ℃ / ${entry.envRh} %
                """.trimIndent()
            )
        )

        root.addView(batchCard)
        root.addView(UiKit.spacer(context))

        val timelineCard = UiKit.card(context)

        timelineCard.addView(UiKit.cardTitle(context, "TIMELINE"))
        timelineCard.addView(
            UiKit.bodyText(
                context,
                """
Turning
${formatSec(entry.actualTurningSec ?: entry.predictedTurningSec)}

Yellow
${formatSec(entry.actualYellowSec ?: entry.predictedYellowSec)}

First Crack
${formatSec(entry.actualFcSec ?: entry.predictedFcSec)}

Drop
${formatSec(entry.actualDropSec ?: entry.predictedDropSec)}
                """.trimIndent()
            )
        )

        root.addView(timelineCard)
        root.addView(UiKit.spacer(context))

        val reportCard = UiKit.card(context)

        reportCard.addView(UiKit.cardTitle(context, "REPORT"))
        reportCard.addView(
            UiKit.bodyText(
                context,
                entry.reportText
            )
        )

        root.addView(reportCard)
        root.addView(UiKit.spacer(context))

        val diagnosisCard = UiKit.card(context)

        diagnosisCard.addView(UiKit.cardTitle(context, "DIAGNOSIS"))
        diagnosisCard.addView(
            UiKit.bodyText(
                context,
                entry.diagnosisText
            )
        )

        root.addView(diagnosisCard)
        root.addView(UiKit.spacer(context))

        val correctionCard = UiKit.card(context)

        correctionCard.addView(UiKit.cardTitle(context, "CORRECTION"))
        correctionCard.addView(
            UiKit.bodyText(
                context,
                entry.correctionText
            )
        )

        root.addView(correctionCard)
        root.addView(UiKit.spacer(context))

        val evaluationCard = UiKit.card(context)

        val beanColorInput = decimalInput(context, "Bean Color")
        val groundColorInput = decimalInput(context, "Ground Color")
        val roastedAwInput = decimalInput(context, "Roasted AW")

        val sweetnessInput = integerInput(context, "Sweetness")
        val acidityInput = integerInput(context, "Acidity")
        val bodyInput = integerInput(context, "Body")
        val flavorClarityInput = integerInput(context, "Flavor Clarity")
        val balanceInput = integerInput(context, "Balance")

        val notesInput = EditText(context).apply {
            hint = "Notes"
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            minLines = 3
        }

        fillEvaluation(
            entry.evaluation,
            beanColorInput,
            groundColorInput,
            roastedAwInput,
            sweetnessInput,
            acidityInput,
            bodyInput,
            flavorClarityInput,
            balanceInput,
            notesInput
        )

        val saveEvaluationBtn = UiKit.primaryButton(context, "SAVE EVALUATION")
        val clearEvaluationBtn = UiKit.secondaryButton(context, "CLEAR EVALUATION")

        evaluationCard.addView(UiKit.cardTitle(context, "EVALUATION"))
        evaluationCard.addView(beanColorInput)
        evaluationCard.addView(groundColorInput)
        evaluationCard.addView(roastedAwInput)
        evaluationCard.addView(sweetnessInput)
        evaluationCard.addView(acidityInput)
        evaluationCard.addView(bodyInput)
        evaluationCard.addView(flavorClarityInput)
        evaluationCard.addView(balanceInput)
        evaluationCard.addView(notesInput)
        evaluationCard.addView(UiKit.spacer(context))
        evaluationCard.addView(
            UiKit.bodyText(
                context,
                buildEvaluationSummary(entry.evaluation)
            )
        )
        evaluationCard.addView(UiKit.spacer(context))
        evaluationCard.addView(saveEvaluationBtn)
        evaluationCard.addView(clearEvaluationBtn)

        root.addView(evaluationCard)
        root.addView(UiKit.spacer(context))

        val styleCard = UiKit.card(context)
        val createStyleBtn = UiKit.primaryButton(context, "CREATE MY STYLE")

        styleCard.addView(UiKit.cardTitle(context, "STYLE"))
        styleCard.addView(createStyleBtn)

        root.addView(styleCard)
        root.addView(UiKit.spacer(context))

        val dangerCard = UiKit.card(context)
        val deleteBtn = UiKit.secondaryButton(context, "DELETE THIS HISTORY")

        dangerCard.addView(UiKit.cardTitle(context, "DANGER"))
        dangerCard.addView(
            UiKit.bodyText(
                context,
                "Delete this roast history entry from local storage."
            )
        )
        dangerCard.addView(UiKit.spacer(context))
        dangerCard.addView(deleteBtn)

        root.addView(dangerCard)

        saveEvaluationBtn.setOnClickListener {
            val evaluation = RoastEvaluation(
                beanColor = parseDoubleOrNull(beanColorInput.text?.toString()),
                groundColor = parseDoubleOrNull(groundColorInput.text?.toString()),
                roastedAw = parseDoubleOrNull(roastedAwInput.text?.toString()),
                sweetness = parseIntOrNull(sweetnessInput.text?.toString()),
                acidity = parseIntOrNull(acidityInput.text?.toString()),
                body = parseIntOrNull(bodyInput.text?.toString()),
                flavorClarity = parseIntOrNull(flavorClarityInput.text?.toString()),
                balance = parseIntOrNull(balanceInput.text?.toString()),
                notes = notesInput.text?.toString()?.trim().orEmpty()
            )

            val result = RoastHistoryEngine.saveEvaluation(entry.batchId, evaluation)

            Toast.makeText(
                context,
                result.message,
                Toast.LENGTH_LONG
            ).show()

            val updatedEntry = RoastHistoryEngine.findByBatchId(entry.batchId)
            show(
                context = context,
                container = container,
                entry = updatedEntry,
                onBack = onBack
            )
        }

        clearEvaluationBtn.setOnClickListener {
            if (entry.evaluation == null) {
                Toast.makeText(context, "No evaluation to clear", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(context)
                .setTitle("Clear evaluation?")
                .setMessage("Saved evaluation data for this roast will be removed.")
                .setPositiveButton("CLEAR") { _, _ ->
                    val result = RoastHistoryEngine.clearEvaluation(entry.batchId)

                    Toast.makeText(
                        context,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()

                    val updatedEntry = RoastHistoryEngine.findByBatchId(entry.batchId)
                    show(
                        context = context,
                        container = container,
                        entry = updatedEntry,
                        onBack = onBack
                    )
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }

        createStyleBtn.setOnClickListener {
            val suggestedName =
                RoastStyleFromBatchEngine.suggestStyleName(entry.batchId)

            val result =
                RoastStyleFromBatchEngine.createFromBatch(
                    entry.batchId,
                    suggestedName
                )

            Toast.makeText(
                context,
                result.message,
                Toast.LENGTH_LONG
            ).show()
        }

        deleteBtn.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete this history?")
                .setMessage("Batch ${entry.batchId} will be permanently removed from local history.")
                .setPositiveButton("DELETE") { _, _ ->
                    val result = RoastHistoryEngine.delete(entry.batchId)

                    Toast.makeText(
                        context,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()

                    if (result.deleted) {
                        onBack?.invoke() ?: RoastStudioPage.show(context, container)
                    }
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }

        backBtn.setOnClickListener {
            onBack?.invoke() ?: RoastStudioPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildSummaryStrip(
        entry: RoastHistoryEntry
    ): String {
        return """
Batch
${entry.batchId}

Status
${entry.batchStatus}

Health
${entry.roastHealthHeadline}

Evaluation
${if (entry.evaluation != null) "Saved" else "Not saved"}

FC / Drop
${formatSec(entry.actualFcSec ?: entry.predictedFcSec)} / ${formatSec(entry.actualDropSec ?: entry.predictedDropSec)}

Pre-FC RoR
${formatRor(entry.actualPreFcRor)}
        """.trimIndent()
    }

    private fun decimalInput(
        context: Context,
        hint: String
    ): EditText {
        return EditText(context).apply {
            this.hint = hint
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine(true)
        }
    }

    private fun integerInput(
        context: Context,
        hint: String
    ): EditText {
        return EditText(context).apply {
            this.hint = hint
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }
    }

    private fun fillEvaluation(
        evaluation: RoastEvaluation?,
        beanColorInput: EditText,
        groundColorInput: EditText,
        roastedAwInput: EditText,
        sweetnessInput: EditText,
        acidityInput: EditText,
        bodyInput: EditText,
        flavorClarityInput: EditText,
        balanceInput: EditText,
        notesInput: EditText
    ) {
        beanColorInput.setText(evaluation?.beanColor?.toString().orEmpty())
        groundColorInput.setText(evaluation?.groundColor?.toString().orEmpty())
        roastedAwInput.setText(evaluation?.roastedAw?.toString().orEmpty())
        sweetnessInput.setText(evaluation?.sweetness?.toString().orEmpty())
        acidityInput.setText(evaluation?.acidity?.toString().orEmpty())
        bodyInput.setText(evaluation?.body?.toString().orEmpty())
        flavorClarityInput.setText(evaluation?.flavorClarity?.toString().orEmpty())
        balanceInput.setText(evaluation?.balance?.toString().orEmpty())
        notesInput.setText(evaluation?.notes.orEmpty())
    }

    private fun buildEvaluationSummary(
        evaluation: RoastEvaluation?
    ): String {
        if (evaluation == null) {
            return """
Saved Evaluation
No evaluation saved yet.
            """.trimIndent()
        }

        return """
Saved Evaluation

Bean Color
${evaluation.beanColor ?: "-"}

Ground Color
${evaluation.groundColor ?: "-"}

Roasted AW
${evaluation.roastedAw ?: "-"}

Sweetness
${evaluation.sweetness ?: "-"}

Acidity
${evaluation.acidity ?: "-"}

Body
${evaluation.body ?: "-"}

Flavor Clarity
${evaluation.flavorClarity ?: "-"}

Balance
${evaluation.balance ?: "-"}

Notes
${evaluation.notes.ifBlank { "-" }}
        """.trimIndent()
    }

    private fun parseDoubleOrNull(value: String?): Double? {
        val text = value?.trim().orEmpty()
        if (text.isBlank()) return null
        return text.toDoubleOrNull()
    }

    private fun parseIntOrNull(value: String?): Int? {
        val text = value?.trim().orEmpty()
        if (text.isBlank()) return null
        return text.toIntOrNull()
    }

    private fun formatSec(sec: Int?): String {
        if (sec == null) return "-"
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatRor(value: Double?): String {
        if (value == null) return "-"
        return String.format(Locale.getDefault(), "%.1f ℃/min", value)
    }

    private fun formatDateTime(ms: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(ms))
    }
}
