package com.roastos.app.ui

import android.content.Context
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView

object UiKit {

    const val PAGE_PADDING = 24
    const val CARD_PADDING = 24
    const val SECTION_GAP = 24
    const val INNER_GAP = 16

    fun pageRoot(context: Context): LinearLayout {
        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(PAGE_PADDING, PAGE_PADDING, PAGE_PADDING, PAGE_PADDING)
        return root
    }

    fun card(context: Context): LinearLayout {
        val card = LinearLayout(context)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)
        return card
    }

    fun pageTitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 24f
        view.setTypeface(null, Typeface.BOLD)
        return view
    }

    fun pageSubtitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 14f
        return view
    }

    fun cardTitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 18f
        view.setTypeface(null, Typeface.BOLD)
        return view
    }

    fun bodyText(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 15f
        view.setPadding(0, INNER_GAP, 0, 0)
        return view
    }

    fun spacer(context: Context, height: Int = SECTION_GAP): TextView {
        val view = TextView(context)
        view.text = ""
        view.setPadding(0, height, 0, 0)
        return view
    }

    fun buildCard(
        context: Context,
        heading: String,
        content: String
    ): LinearLayout {
        val card = card(context)
        card.addView(cardTitle(context, heading))
        card.addView(bodyText(context, content))
        return card
    }
}
