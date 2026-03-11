package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.*

object RoastEvaluationPage {

    fun show(
        context: Context,
        container: LinearLayout,
        entry: RoastHistoryEntry?
    ) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST EVALUATION"))
        root.addView(UiKit.pageSubtitle(context, "Post roast cup / color / aw input"))
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK TO DETAIL")
        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        if (entry == null) {

            val empty = UiKit.card(context)

            empty.addView(UiKit.cardTitle(context, "NO BATCH"))
            empty.addView(
                UiKit.bodyText(
                    context,
                    "No roast batch selected."
                )
            )

            root.addView(empty)

            backBtn.setOnClickListener {
                HistoryDetailPage.show(context, container, null)
            }

            scroll.addView(root)
            container.addView(scroll)
            return
        }

        val beanColorInput = decimalInput(context, "Bean Color (Agtron)")
        val groundColorInput = decimalInput(context, "Ground Color (Agtron)")
        val awInput = decimalInput(context, "Roasted Aw")

        val sweetnessInput = intInput(context, "Sweetness (0-10)")
        val acidityInput = intInput(context, "Acidity (0-10)")
        val bodyInput = intInput(context, "Body (0-10)")
        val clarityInput = intInput(context, "Flavor Clarity (0-10)")
        val balanceInput = intInput(context, "Balance (0-10)")

        val notesInput = EditText(context)
        notesInput.hint = "Cup notes"
        notesInput.minLines = 3

        entry.evaluation?.let { evaluation ->
            beanColorInput.setText(evaluation.beanColor?.toString() ?: "")
            groundColorInput.setText(evaluation.groundColor?.toString() ?: "")
            awInput.setText(evaluation.roastedAw?.toString() ?: "")
            sweetnessInput.setText(evaluation.sweetness?.toString() ?: "")
            acidityInput.setText(evaluation.acidity?.toString() ?: "")
            bodyInput.setText(evaluation.body?.toString() ?: "")
            clarityInput.setText(evaluation.flavorClarity?.toString() ?: "")
            balanceInput.setText(evaluation.balance?.toString() ?: "")
            notesInput.setText(evaluation.notes)
        }

        val saveBtn = Button(context)
        saveBtn.text = "SAVE EVALUATION"

        val resultText = UiKit.bodyText(context, "")

        val card = UiKit.card(context)

        card.addView(UiKit.cardTitle(context, "CUP INPUT"))
        card.addView(
            UiKit.bodyText(
                context,
                """
Batch
${entry.batchId}

Title
${entry.title}
                """.trimIndent()
            )
        )
        card.addView(UiKit.spacer(context))

        card.addView(beanColorInput)
        card.addView(groundColorInput)
        card.addView(awInput)

        card.addView(sweetnessInput)
        card.addView(acidityInput)
        card.addView(bodyInput)
        card.addView(clarityInput)
        card.addView(balanceInput)

        card.addView(notesInput)
        card.addView(UiKit.spacer(context))
        card.addView(saveBtn)
        card.addView(UiKit.spacer(context))
        card.addView(resultText)

        root.addView(card)

        saveBtn.setOnClickListener {

            val beanColor = beanColorInput.text.toString().toDoubleOrNull()
            val groundColor = groundColorInput.text.toString().toDoubleOrNull()
            val roastedAw = awInput.text.toString().toDoubleOrNull()

            val sweetness = sweetnessInput.text.toString().toIntOrNull()
            val acidity = acidityInput.text.toString().toIntOrNull()
            val body = bodyInput.text.toString().toIntOrNull()
            val clarity = clarityInput.text.toString().toIntOrNull()
            val balance = balanceInput.text.toString().toIntOrNull()
            val notes = notesInput.text.toString()

            val evaluation = RoastEvaluation(
                beanColor = beanColor,
                groundColor = groundColor,
                roastedAw = roastedAw,
                sweetness = sweetness,
                acidity = acidity,
                body = body,
                flavorClarity = clarity,
                balance = balance,
                notes = notes
            )

            val saveResult = RoastHistoryEngine.saveEvaluation(
                entry.batchId,
                evaluation
            )

            RoastRiskEventEngine.attachCupResult(
                batchId = entry.batchId,
                beanColor = beanColor,
                groundColor = groundColor,
                aw = roastedAw,
                cupScore = buildCupScore(
                    sweetness = sweetness,
                    acidity = acidity,
                    body = body,
                    clarity = clarity,
                    balance = balance
                ),
                notes = notes
            )

            resultText.text =
                """
Saved

${saveResult.message}

Risk events updated with cup result.
Learning will update automatically.
                """.trimIndent()
        }

        backBtn.setOnClickListener {
            HistoryDetailPage.show(context, container, RoastHistoryEngine.findByBatchId(entry.batchId))
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun decimalInput(
        context: Context,
        hint: String
    ): EditText {

        val input = EditText(context)

        input.hint = hint
        input.inputType =
            InputType.TYPE_CLASS_NUMBER or
            InputType.TYPE_NUMBER_FLAG_DECIMAL

        return input
    }

    private fun intInput(
        context: Context,
        hint: String
    ): EditText {

        val input = EditText(context)

        input.hint = hint
        input.inputType =
            InputType.TYPE_CLASS_NUMBER

        return input
    }

    private fun buildCupScore(
        sweetness: Int?,
        acidity: Int?,
        body: Int?,
        clarity: Int?,
        balance: Int?
    ): Int? {
        val values = listOf(sweetness, acidity, body, clarity, balance).filterNotNull()
        if (values.isEmpty()) return null
        return values.sum() / values.size
    }
}
