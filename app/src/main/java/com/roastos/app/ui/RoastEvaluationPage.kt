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

        val saveBtn = Button(context)
        saveBtn.text = "SAVE EVALUATION"

        val resultText = UiKit.bodyText(context, "")

        val card = UiKit.card(context)

        card.addView(UiKit.cardTitle(context, "CUP INPUT"))

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
        card.addView(resultText)

        root.addView(card)

        saveBtn.setOnClickListener {

            val evaluation = RoastEvaluation(
                beanColor = beanColorInput.text.toString().toDoubleOrNull(),
                groundColor = groundColorInput.text.toString().toDoubleOrNull(),
                roastedAw = awInput.text.toString().toDoubleOrNull(),
                sweetness = sweetnessInput.text.toString().toIntOrNull(),
                acidity = acidityInput.text.toString().toIntOrNull(),
                body = bodyInput.text.toString().toIntOrNull(),
                flavorClarity = clarityInput.text.toString().toIntOrNull(),
                balance = balanceInput.text.toString().toIntOrNull(),
                notes = notesInput.text.toString()
            )

            val result = RoastHistoryEngine.saveEvaluation(
                entry.batchId,
                evaluation
            )

            resultText.text =
                """
Saved

${result.message}

Learning will update automatically.
                """.trimIndent()
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
}
