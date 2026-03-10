package com.roastos.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object UiKit {

    private const val PAGE_PADDING = 36
    private const val CARD_PADDING = 28
    private const val CARD_RADIUS = 28f
    private const val BUTTON_RADIUS = 22f

    private val pageBackgroundColor = Color.rgb(242, 242, 240)
    private val cardBackgroundColor = Color.rgb(236, 236, 232)
    private val buttonBackgroundColor = Color.rgb(214, 214, 210)
    private val titleColor = Color.rgb(86, 86, 82)
    private val bodyColor = Color.rgb(108, 108, 104)
    private val subtleColor = Color.rgb(150, 150, 145)
    private val borderColor = Color.rgb(222, 222, 218)

    fun pageRoot(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(PAGE_PADDING, PAGE_PADDING, PAGE_PADDING, PAGE_PADDING)
        layout.setBackgroundColor(pageBackgroundColor)
        return layout
    }

    fun pageTitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 24f
        view.setTextColor(titleColor)
        view.setTypeface(null, Typeface.BOLD)
        return view
    }

    fun pageSubtitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 13.5f
        view.setTextColor(subtleColor)
        return view
    }

    fun card(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = CARD_RADIUS
            setColor(cardBackgroundColor)
            setStroke(1, borderColor)
        }

        layout.background = bg
        return layout
    }

    fun cardTitle(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 16f
        view.setTextColor(titleColor)
        view.setTypeface(null, Typeface.BOLD)
        return view
    }

    fun bodyText(context: Context, text: String): TextView {
        val view = TextView(context)
        view.text = text
        view.textSize = 13.5f
        view.setTextColor(bodyColor)
        view.setLineSpacing(8f, 1.0f)
        return view
    }

    fun spacer(context: Context, height: Int = 22): TextView {
        val view = TextView(context)
        view.text = ""
        view.height = height
        return view
    }

    fun primaryButton(context: Context, text: String): Button {
        val button = Button(context)
        button.text = text
        button.textSize = 15f
        button.setTextColor(titleColor)
        button.setTypeface(null, Typeface.BOLD)
        button.gravity = Gravity.CENTER

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = BUTTON_RADIUS
            setColor(buttonBackgroundColor)
            setStroke(1, borderColor)
        }

        button.background = bg

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 16
        button.layoutParams = params
        button.setPadding(18, 26, 18, 26)

        return button
    }

    fun secondaryButton(context: Context, text: String): Button {
        val button = Button(context)
        button.text = text
        button.textSize = 14f
        button.setTextColor(bodyColor)
        button.setTypeface(null, Typeface.BOLD)
        button.gravity = Gravity.CENTER

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = BUTTON_RADIUS
            setColor(Color.rgb(226, 226, 222))
            setStroke(1, borderColor)
        }

        button.background = bg

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 14
        button.layoutParams = params
        button.setPadding(18, 22, 18, 22)

        return button
    }
}
