package com.roastos.app.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView

object UiKit {

    const val PAGE_PADDING = 24
    const val CARD_PADDING = 22
    const val INNER_GAP = 14
    const val SECTION_GAP = 18
    const val PAGE_GAP = 24

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
            setTextColor(Color.parseColor("#202124"))
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            setLineSpacing(0f, 1.08f)
        }
    }

    fun pageSubtitle(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor("#5F6368"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setLineSpacing(0f, 1.18f)
            setPadding(0, 8, 0, 0)
        }
    }

    fun card(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)
            setBackgroundColor(Color.parseColor("#F4F5F7"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 0)
            }
        }
    }

    fun cardTitle(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor("#3C4043"))
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setLineSpacing(0f, 1.08f)
            setPadding(0, 0, 0, 10)
        }
    }

    fun bodyText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor("#202124"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f)
            setLineSpacing(0f, 1.24f)
        }
    }

    fun captionText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.parseColor("#5F6368"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
            setLineSpacing(0f, 1.15f)
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
