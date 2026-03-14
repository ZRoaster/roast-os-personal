package com.roastos.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

object UiKit {

    private const val PAGE_PADDING = 32
    private const val CARD_PADDING = 22
    private const val BUTTON_HEIGHT = 92
    private const val FIELD_HEIGHT = 88

    private val cardBackgroundColor = Color.parseColor("#F7F7F5")
    private val cardStrokeColor = Color.parseColor("#DDDCD6")

    private val primaryButtonBackgroundColor = Color.parseColor("#2F2F2B")
    private val primaryButtonTextColor = Color.parseColor("#FFFFFF")

    private val secondaryButtonBackgroundColor = Color.parseColor("#ECEBE6")
    private val secondaryButtonStrokeColor = Color.parseColor("#D8D6CF")
    private val secondaryButtonTextColor = Color.parseColor("#2E2E2A")

    private val fieldBackgroundColor = Color.parseColor("#FCFCFA")
    private val fieldStrokeColor = Color.parseColor("#D8D6CF")
    private val fieldTextColor = Color.parseColor("#24241F")
    private val fieldHintColor = Color.parseColor("#77746D")

    fun pageRoot(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(PAGE_PADDING, PAGE_PADDING, PAGE_PADDING, PAGE_PADDING)
        return layout
    }

    fun pageTitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 22f
        view.setTypeface(null, Typeface.BOLD)
        view.setTextColor(Color.parseColor("#1F1F1B"))
        return view
    }

    fun pageSubtitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 13f
        view.alpha = 0.7f
        view.setTextColor(Color.parseColor("#2A2A26"))
        view.setLineSpacing(2f, 1.0f)
        return view
    }

    fun card(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)

        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(cardBackgroundColor)
            setStroke(2, cardStrokeColor)
        }

        layout.background = background
        return layout
    }

    fun cardTitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 15f
        view.setTypeface(null, Typeface.BOLD)
        view.setTextColor(Color.parseColor("#20201C"))
        view.setLineSpacing(2f, 1.0f)
        return view
    }

    fun sectionLabel(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 11f
        view.setTypeface(null, Typeface.BOLD)
        view.alpha = 0.74f
        view.setTextColor(Color.parseColor("#34342F"))
        return view
    }

    fun bodyText(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 13f
        view.setTextColor(Color.parseColor("#24241F"))
        view.setLineSpacing(4f, 1.0f)
        return view
    }

    fun helperText(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 12f
        view.alpha = 0.84f
        view.setTextColor(Color.parseColor("#33332E"))
        view.setLineSpacing(3f, 1.0f)
        return view
    }

    fun dangerText(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 12f
        view.alpha = 0.78f
        view.setTextColor(Color.parseColor("#5A403E"))
        view.setLineSpacing(3f, 1.0f)
        return view
    }

    fun primaryButton(context: Context, text: String): Button {
        val button = Button(context)
        button.text = text
        button.textSize = 14f
        button.setTypeface(null, Typeface.BOLD)
        button.setTextColor(primaryButtonTextColor)
        button.minHeight = BUTTON_HEIGHT
        button.minimumHeight = BUTTON_HEIGHT
        button.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(primaryButtonBackgroundColor)
        }
        return button
    }

    fun secondaryButton(context: Context, text: String): Button {
        val button = Button(context)
        button.text = text
        button.textSize = 13f
        button.setTextColor(secondaryButtonTextColor)
        button.minHeight = BUTTON_HEIGHT
        button.minimumHeight = BUTTON_HEIGHT
        button.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(secondaryButtonBackgroundColor)
            setStroke(2, secondaryButtonStrokeColor)
        }
        return button
    }

    fun decimalField(context: Context, hint: String): EditText {
        return baseField(context, hint).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }

    fun integerField(context: Context, hint: String): EditText {
        return baseField(context, hint).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    fun notesField(context: Context, hint: String): EditText {
        return baseField(context, hint).apply {
            minLines = 4
            setLines(4)
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
    }

    private fun baseField(context: Context, hint: String): EditText {
        val field = EditText(context)
        field.hint = hint
        field.textSize = 13f
        field.setTextColor(fieldTextColor)
        field.setHintTextColor(fieldHintColor)
        field.minHeight = FIELD_HEIGHT
        field.minimumHeight = FIELD_HEIGHT
        field.setPadding(24, 20, 24, 20)
        field.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 18f
            setColor(fieldBackgroundColor)
            setStroke(2, fieldStrokeColor)
        }
        return field
    }

    fun spacer(context: Context): TextView {
        val view = TextView(context)
        view.text = ""
        view.height = 18
        return view
    }
}
