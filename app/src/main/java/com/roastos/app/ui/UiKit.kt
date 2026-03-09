package com.roastos.app.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView

object UiKit {

    const val PAGE_PADDING = 24
    const val CARD_PADDING = 22
    const val INNER_GAP = 14
    const val SECTION_GAP = 18
    const val PAGE_GAP = 24
    const val BUTTON_GAP = 10

    private const val COLOR_TEXT_PRIMARY = "#202124"
    private const val COLOR_TEXT_SECONDARY = "#5F6368"
    private const val COLOR_TEXT_MUTED = "#80868B"

    private const val COLOR_CARD = "#F4F5F7"
    private const val COLOR_CARD_ALT = "#ECEFF3"

    private const val COLOR_BTN_PRIMARY_BG = "#202124"
    private const val COLOR_BTN_PRIMARY_TEXT = "#FFFFFF"

    private const val COLOR_BTN_SECONDARY_BG = "#E6E6E6"
    private const val COLOR_BTN_SECONDARY_TEXT = "#202124"

    private const val COLOR_BTN_DANGER_BG = "#D93025"
    private const val COLOR_BTN_DANGER_TEXT = "#FFFFFF"

    fun pageRoot(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(PAGE_PADDING, PAGE_PADDING, PAGE_PADDING, PAGE_PADDING)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun pageTitle(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            setLineSpacing(0f, 1.08f)
            setPadding(0, 0, 0, 4)
        }
    }

    fun pageSubtitle(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setLineSpacing(0f, 1.18f)
            setPadding(0, 6, 0, 0)
        }
    }

    fun card(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)
            setBackgroundColor(Color.parseColor(COLOR_CARD))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun cardAlt(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)
            setBackgroundColor(Color.parseColor(COLOR_CARD_ALT))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun cardTitle(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor("#3C4043"))
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setLineSpacing(0f, 1.08f)
            setPadding(0, 0, 0, 12)
        }
    }

    fun sectionLabel(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY))
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(0, 0, 0, 8)
        }
    }

    fun bodyText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
            setLineSpacing(0f, 1.24f)
        }
    }

    fun captionText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
            setLineSpacing(0f, 1.15f)
        }
    }

    fun mutedText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor(COLOR_TEXT_MUTED))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setLineSpacing(0f, 1.12f)
        }
    }

    fun centeredLabel(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor("#3C4043"))
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            gravity = Gravity.CENTER
        }
    }

    fun primaryButton(context: Context, text: String): Button {
        return baseButton(context, text).apply {
            setBackgroundColor(Color.parseColor(COLOR_BTN_PRIMARY_BG))
            setTextColor(Color.parseColor(COLOR_BTN_PRIMARY_TEXT))
        }
    }

    fun secondaryButton(context: Context, text: String): Button {
        return baseButton(context, text).apply {
            setBackgroundColor(Color.parseColor(COLOR_BTN_SECONDARY_BG))
            setTextColor(Color.parseColor(COLOR_BTN_SECONDARY_TEXT))
        }
    }

    fun dangerButton(context: Context, text: String): Button {
        return baseButton(context, text).apply {
            setBackgroundColor(Color.parseColor(COLOR_BTN_DANGER_BG))
            setTextColor(Color.parseColor(COLOR_BTN_DANGER_TEXT))
        }
    }

    private fun baseButton(context: Context, text: String): Button {
        return Button(context).apply {
            this.text = text
            setAllCaps(false)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
            setTypeface(null, Typeface.BOLD)
            setPadding(24, 18, 24, 18)
            minWidth = 0
            minimumWidth = 0
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = BUTTON_GAP
            }
        }
    }

    fun buildCard(
        context: Context,
        title: String,
        body: String
    ): LinearLayout {
        return card(context).apply {
            addView(cardTitle(context, title))
            addView(bodyText(context, body))
        }
    }

    fun spacer(context: Context, heightDp: Int = PAGE_GAP): Space {
        return Space(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightDp
            )
        }
    }

    fun smallSpacer(context: Context): Space {
        return spacer(context, SECTION_GAP)
    }

    fun tinySpacer(context: Context): Space {
        return spacer(context, INNER_GAP)
    }
}
