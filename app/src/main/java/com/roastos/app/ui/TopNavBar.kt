package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
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
        val bar = UiKit.card(context)

        val title = UiKit.cardTitle(context, "NAVIGATION")
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val operateBtn = if (current == Section.OPERATE) {
            UiKit.primaryButton(context, "OPERATE")
        } else {
            UiKit.secondaryButton(context, "OPERATE")
        }

        val reviewBtn = if (current == Section.REVIEW) {
            UiKit.primaryButton(context, "REVIEW")
        } else {
            UiKit.secondaryButton(context, "REVIEW")
        }

        val studioBtn = if (current == Section.STUDIO) {
            UiKit.primaryButton(context, "STUDIO")
        } else {
            UiKit.secondaryButton(context, "STUDIO")
        }

        val systemBtn = if (current == Section.SYSTEM) {
            UiKit.primaryButton(context, "SYSTEM")
        } else {
            UiKit.secondaryButton(context, "SYSTEM")
        }

        operateBtn.setOnClickListener {
            RoastOperatorPage.show(context, container)
        }

        reviewBtn.setOnClickListener {
            ReviewHubPage.show(
                context = context,
                container = container,
                onBack = {
                    MainShellPage.show(context, container)
                }
            )
        }

        studioBtn.setOnClickListener {
            StudioHubPage.show(
                context = context,
                container = container,
                onBack = {
                    MainShellPage.show(context, container)
                }
            )
        }

        systemBtn.setOnClickListener {
            SystemHubPage.show(
                context = context,
                container = container,
                onBack = {
                    MainShellPage.show(context, container)
                }
            )
        }

        row.addView(operateBtn)
        row.addView(reviewBtn)
        row.addView(studioBtn)
        row.addView(systemBtn)

        bar.addView(title)
        bar.addView(UiKit.spacer(context))
        bar.addView(row)

        return bar
    }
}
