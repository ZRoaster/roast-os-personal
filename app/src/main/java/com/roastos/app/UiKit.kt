package com.roastos.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

object UiKit {

    private const val PAGE_PADDING_DP = 24
    private const val CARD_PADDING_DP = 18
    private const val CARD_RADIUS_DP = 18
    private const val BUTTON_RADIUS_DP = 16
    private const val FIELD_RADIUS_DP = 16

    private const val PRIMARY_BUTTON_HEIGHT_DP = 56
    private const val SECONDARY_BUTTON_HEIGHT_DP = 52
    private const val FIELD_HEIGHT_DP = 52
    private const val NOTES_MIN_HEIGHT_DP = 108

    private const val SPACER_L_DP = 18
    private const val SPACER_M_DP = 12
    private const val SPACER_S_DP = 8

    private val pageBackgroundColor = Color.parseColor("#F3F2EF")
    private val cardBackgroundColor = Color.parseColor("#FAF9F6")
    private val cardStrokeColor = Color.parseColor("#DDD9D2")

    private val primaryTextColor = Color.parseColor("#1E1E1A")
    private val secondaryTextColor = Color.parseColor("#5E5A54")
    private val tertiaryTextColor = Color.parseColor("#7A756D")

    private val primaryButtonBackgroundColor = Color.parseColor("#262622")
    private val primaryButtonTextColor = Color.parseColor("#FFFFFF")

    private val secondaryButtonBackgroundColor = Color.parseColor("#F3F1EC")
    private val secondaryButtonStrokeColor = Color.parseColor("#D8D3CB")
    private val secondaryButtonTextColor = Color.parseColor("#2B2A26")

    private val fieldBackgroundColor = Color.parseColor("#FCFBF8")
    private val fieldStrokeColor = Color.parseColor("#D8D3CB")
    private val fieldTextColor = Color.parseColor("#24241F")
    private val fieldHintColor = Color.parseColor("#8A857D")

    private fun dp(context: Context, value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    fun pageRoot(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(pageBackgroundColor)
            setPadding(
                dp(context, PAGE_PADDING_DP),
                dp(context, PAGE_PADDING_DP),
                dp(context, PAGE_PADDING_DP),
                dp(context, PAGE_PADDING_DP)
            )
        }
    }

    fun pageTitle(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(primaryTextColor)
            setTypeface(null, Typeface.BOLD)
            textSize = 21f
            setLineSpacing(0f, 1.0f)
        }
    }

    fun pageSubtitle(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(secondaryTextColor)
            textSize = 11f
            setLineSpacing(0f, 1.0f)
            alpha = 0.95f
        }
    }

    fun card(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(context, CARD_PADDING_DP),
                dp(context, CARD_PADDING_DP),
                dp(context, CARD_PADDING_DP),
                dp(context, CARD_PADDING_DP)
            )
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(context, CARD_RADIUS_DP).toFloat()
                setColor(cardBackgroundColor)
                setStroke(dp(context, 1), cardStrokeColor)
            }
        }
    }

    fun cardTitle(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(primaryTextColor)
            setTypeface(null, Typeface.BOLD)
            textSize = 13f
            setLineSpacing(0f, 1.0f)
        }
    }

    fun sectionLabel(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(tertiaryTextColor)
            setTypeface(null, Typeface.BOLD)
            textSize = 10f
            setLineSpacing(0f, 1.0f)
        }
    }

    fun bodyText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(primaryTextColor)
            textSize = 12.5f
            setLineSpacing(dp(context, 2).toFloat(), 1.0f)
        }
    }

    fun helperText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(secondaryTextColor)
            textSize = 11f
            setLineSpacing(dp(context, 1).toFloat(), 1.0f)
        }
    }

    fun dangerText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor("#8A4A3C"))
            textSize = 11f
            setLineSpacing(dp(context, 1).toFloat(), 1.0f)
        }
    }

    fun primaryButton(context: Context, text: String): Button {
        return Button(context).apply {
            this.text = text
            setTextColor(primaryButtonTextColor)
            setTypeface(null, Typeface.BOLD)
            textSize = 12.5f
            minHeight = dp(context, PRIMARY_BUTTON_HEIGHT_DP)
            minimumHeight = dp(context, PRIMARY_BUTTON_HEIGHT_DP)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(context, BUTTON_RADIUS_DP).toFloat()
                setColor(primaryButtonBackgroundColor)
            }
            isAllCaps = false
        }
    }

    fun secondaryButton(context: Context, text: String): Button {
        return Button(context).apply {
            this.text = text
            setTextColor(secondaryButtonTextColor)
            textSize = 12f
            minHeight = dp(context, SECONDARY_BUTTON_HEIGHT_DP)
            minimumHeight = dp(context, SECONDARY_BUTTON_HEIGHT_DP)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(context, BUTTON_RADIUS_DP).toFloat()
                setColor(secondaryButtonBackgroundColor)
                setStroke(dp(context, 1), secondaryButtonStrokeColor)
            }
            isAllCaps = false
        }
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
            minHeight = dp(context, NOTES_MIN_HEIGHT_DP)
            minimumHeight = dp(context, NOTES_MIN_HEIGHT_DP)
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
    }

    private fun baseField(context: Context, hint: String): EditText {
        return EditText(context).apply {
            this.hint = hint
            setTextColor(fieldTextColor)
            setHintTextColor(fieldHintColor)
            textSize = 12.5f
            minHeight = dp(context, FIELD_HEIGHT_DP)
            minimumHeight = dp(context, FIELD_HEIGHT_DP)
            setPadding(
                dp(context, 16),
                dp(context, 12),
                dp(context, 16),
                dp(context, 12)
            )
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(context, FIELD_RADIUS_DP).toFloat()
                setColor(fieldBackgroundColor)
                setStroke(dp(context, 1), fieldStrokeColor)
            }
        }
    }

    fun spacer(context: Context): TextView {
        return TextView(context).apply {
            text = ""
            height = dp(context, SPACER_L_DP)
        }
    }

    fun spacerM(context: Context): TextView {
        return TextView(context).apply {
            text = ""
            height = dp(context, SPACER_M_DP)
        }
    }

    fun spacerS(context: Context): TextView {
        return TextView(context).apply {
            text = ""
            height = dp(context, SPACER_S_DP)
        }
    }
}
