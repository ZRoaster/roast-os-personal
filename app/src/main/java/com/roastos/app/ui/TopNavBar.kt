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
        val card = UiKit.card(context)

        card.addView(UiKit.cardTitle(context, "NAVIGATION"))
        card.addView(UiKit.spacer(context))

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val operateTab = navTab(
            context = context,
            text = "OPERATE",
            active = current == Section.OPERATE
        ) {
            RoastOperatorPage.show(context, container)
        }

        val reviewTab = navTab(
            context = context,
            text = "REVIEW",
            active = current == Section.REVIEW
        ) {
            ReviewHubPage.show(
                context = context,
                container = container,
                onBack = {
                    MainShellPage.show(context, container)
                }
            )
        }

        val studioTab = navTab(
            context = context,
            text = "STUDIO",
            active = current == Section.STUDIO
        ) {
            StudioHubPage.show(
                context = context,
                container = container,
                onBack = {
                    MainShellPage.show(context, container)
                }
            )
        }

        val systemTab = navTab(
            context = context,
            text = "SYSTEM",
            active = current == Section.SYSTEM
        ) {
            SystemHubPage.show(
                context = context,
                container = container,
                onBack = {
                    MainShellPage.show(context, container)
                }
            )
        }

        row.addView(operateTab, weightedParams())
        row.addView(reviewTab, weightedParams())
        row.addView(studioTab, weightedParams())
        row.addView(systemTab, weightedParams())

        card.addView(row)
        return card
    }

    private fun navTab(
        context: Context,
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
            setPadding(12, 18, 12, 18)
            gravity = android.view.Gravity.CENTER
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
