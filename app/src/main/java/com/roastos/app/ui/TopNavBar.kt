package com.roastos.app.ui

import android.content.Context
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.UiKit

object TopNavBar {

    enum class Section {
        OPERATE,
        REVIEW,
        STUDIO,
        SYSTEM
    }

    fun create(
        context: Context,
        container: LinearLayout,
        current: Section
    ): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(
            navTab(context, container, "OPERATE", current == Section.OPERATE) {
                RoastOperatorPage.show(context, container)
            },
            weightedParams()
        )

        row.addView(
            navTab(context, container, "REVIEW", current == Section.REVIEW) {
                ReviewHubPage.show(
                    context = context,
                    container = container,
                    onBack = { MainShellPage.show(context, container) }
                )
            },
            weightedParams()
        )

        row.addView(
            navTab(context, container, "STUDIO", current == Section.STUDIO) {
                StudioHubPage.show(
                    context = context,
                    container = container,
                    onBack = { MainShellPage.show(context, container) }
                )
            },
            weightedParams()
        )

        row.addView(
            navTab(context, container, "SYSTEM", current == Section.SYSTEM) {
                SystemHubPage.show(
                    context = context,
                    container = container,
                    onBack = { MainShellPage.show(context, container) }
                )
            },
            weightedParams()
        )

        return row
    }

    private fun navTab(
        context: Context,
        container: LinearLayout,
        text: String,
        active: Boolean,
        onClick: () -> Unit
    ): TextView {
        val view = if (active) {
            UiKit.cardTitle(context, text)
        } else {
            UiKit.helperText(context, text)
        }

        view.apply {
            gravity = android.view.Gravity.CENTER
            setPadding(8, 10, 8, 10)
            if (active) {
                setTypeface(null, Typeface.BOLD)
            }
            setOnClickListener { onClick() }
        }

        return view
    }

    private fun weightedParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
    }
}
