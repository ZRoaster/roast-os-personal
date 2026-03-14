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

    private const val PAGE_PADDING = 40
    private const val CARD_PADDING = 30
    private const val BUTTON_HEIGHT = 120
    private const val FIELD_HEIGHT = 112

    private val cardBackgroundColor = Color.parseColor("#F7F7F5")
    private val cardStrokeColor = Color.parseColor("#DDDCD6")

    private val primaryButtonBackgroundColor = Color.parseColor("#2F2F2B")
    private val primaryButtonTextColor = Color.parseColor("#FFFFFF")

    private val secondaryButtonBackgroundColor = Color.parseColor("#ECEBE6")
    private val secondaryButtonStrokeColor = Color.parseColor("#D8D6CF")
    private val secondaryButtonTextColor = Color.parseColor("#2E2E2A")

    private val fieldBackgroundColor = Color.parseColor("#FBFBF9")
    private val fieldStrokeColor = Color.parseColor("#D6D4CC")
    private val fieldTextColor = Color.parseColor("#24241F")
    private val fieldHintColor = Color.parseColor("#7A786F")

    fun pageRoot(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(PAGE_PADDING, PAGE_PADDING, PAGE_PADDING, PAGE_PADDING)
        return layout
    }

    fun pageTitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 24f
        view.setTypeface(null, Typeface.BOLD)
        view.setTextColor(Color.parseColor("#1F1F1B"))
        return view
    }

    fun pageSubtitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 14f
        view.alpha = 0.68f
        view.setTextColor(Color.parseColor("#2A2A26"))
        return view
    }

    fun card(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)

        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 26f
            setColor(cardBackgroundColor)
            setStroke(2, cardStrokeColor)
        }

        layout.background = background
        return layout
    }

    fun cardTitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 16f
        view.setTypeface(null, Typeface.BOLD)
        view.setTextColor(Color.parseColor("#20201C"))
        return view
    }

    fun sectionLabel(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 12f
        view.setTypeface(null, Typeface.BOLD)
        view.alpha = 0.74f
        view.setTextColor(Color.parseColor("#34342F"))
        return view
    }

    fun bodyText(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 14f
        view.setTextColor(Color.parseColor("#24241F"))
        view.setLineSpacing(8f, 1.08f)
        return view
    }

    fun helperText(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 13f
        view.alpha = 0.84f
        view.setTextColor(Color.parseColor("#33332E"))
        view.setLineSpacing(6f, 1.06f)
        return view
    }

    fun dangerText(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 13f
        view.alpha = 0.76f
        view.setTextColor(Color.parseColor("#5A403E"))
        view.setLineSpacing(6f, 1.06f)
        return view
    }

    fun decimalField(context: Context, hint: String): EditText {
        return styledField(context).apply {
            this.hint = hint
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine(true)
        }
    }

    fun integerField(context: Context, hint: String): EditText {
        return styledField(context).apply {
            this.hint = hint
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }
    }

    fun notesField(context: Context, hint: String = "Notes"): EditText {
        return styledField(context).apply {
            this.hint = hint
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            minLines = 4
            setSingleLine(false)
        }
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
            cornerRadius = 22f
            setColor(primaryButtonBackgroundColor)
        }
        return button
    }

    fun secondaryButton(context: Context, text: String): Button {
        val button = Button(context)
        button.text = text
        button.textSize = 14f
        button.setTextColor(secondaryButtonTextColor)
        button.minHeight = BUTTON_HEIGHT
        button.minimumHeight = BUTTON_HEIGHT
        button.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 22f
            setColor(secondaryButtonBackgroundColor)
            setStroke(2, secondaryButtonStrokeColor)
        }
        return button
    }

    fun spacer(context: Context): TextView {
        val view = TextView(context)
        view.text = ""
        view.height = 28
        return view
    }

    private fun styledField(context: Context): EditText {
        return EditText(context).apply {
            textSize = 14f
            setTextColor(fieldTextColor)
            setHintTextColor(fieldHintColor)
            minHeight = FIELD_HEIGHT
            minimumHeight = FIELD_HEIGHT
            setPadding(28, 22, 28, 22)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(fieldBackgroundColor)
                setStroke(2, fieldStrokeColor)
            }
        }
    }
}
