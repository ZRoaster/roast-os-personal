package com.roastos.app

import android.content.Context
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object UiKit {

    fun pageRoot(context: Context): LinearLayout {

        val layout = LinearLayout(context)

        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 40, 40, 40)

        return layout
    }

    fun pageTitle(context: Context, text: String): TextView {

        val view = TextView(context)

        view.text = text
        view.textSize = 22f
        view.setTypeface(null, Typeface.BOLD)

        return view
    }

    fun pageSubtitle(context: Context, text: String): TextView {

        val view = TextView(context)

        view.text = text
        view.textSize = 14f
        view.alpha = 0.7f

        return view
    }

    fun card(context: Context): LinearLayout {

        val layout = LinearLayout(context)

        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 30, 30, 30)

        return layout
    }

    fun cardTitle(context: Context, text: String): TextView {

        val view = TextView(context)

        view.text = text
        view.textSize = 16f
        view.setTypeface(null, Typeface.BOLD)

        return view
    }

    fun bodyText(context: Context, text: String): TextView {

        val view = TextView(context)

        view.text = text
        view.textSize = 14f

        return view
    }

    fun primaryButton(context: Context, text: String): Button {

        val button = Button(context)

        button.text = text

        return button
    }

    fun secondaryButton(context: Context, text: String): Button {

        val button = Button(context)

        button.text = text
        button.alpha = 0.85f

        return button
    }

    fun spacer(context: Context): TextView {

        val view = TextView(context)

        view.text = ""
        view.height = 40

        return view
    }
}
