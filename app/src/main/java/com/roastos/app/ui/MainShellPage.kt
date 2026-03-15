package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.UiKit

object MainShellPage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROASTOS"))
        root.addView(UiKit.pageSubtitle(context, "Roast operation, review, and system building"))
        root.addView(UiKit.spacer(context))

        val operateCard = UiKit.card(context)
        val openOperateBtn = UiKit.primaryButton(context, "Open Operate")
        operateCard.addView(UiKit.cardTitle(context, "OPERATE"))
        operateCard.addView(UiKit.helperText(context, "Live roast control and decision."))
        operateCard.addView(UiKit.spacerM(context))
        operateCard.addView(openOperateBtn)
        root.addView(operateCard)
        root.addView(UiKit.spacer(context))

        val reviewCard = UiKit.card(context)
        val openReviewBtn = UiKit.secondaryButton(context, "Open Review")
        reviewCard.addView(UiKit.cardTitle(context, "REVIEW"))
        reviewCard.addView(UiKit.helperText(context, "Inspect batches and compare results."))
        reviewCard.addView(UiKit.spacerM(context))
        reviewCard.addView(openReviewBtn)
        root.addView(reviewCard)
        root.addView(UiKit.spacer(context))

        val studioCard = UiKit.card(context)
        val openStudioBtn = UiKit.secondaryButton(context, "Open Studio")
        studioCard.addView(UiKit.cardTitle(context, "STUDIO"))
        studioCard.addView(UiKit.helperText(context, "Prepare environment, style, and setup."))
        studioCard.addView(UiKit.spacerM(context))
        studioCard.addView(openStudioBtn)
        root.addView(studioCard)
        root.addView(UiKit.spacer(context))

        val systemCard = UiKit.card(context)
        val openSystemBtn = UiKit.secondaryButton(context, "Open System")
        systemCard.addView(UiKit.cardTitle(context, "SYSTEM"))
        systemCard.addView(UiKit.helperText(context, "Profile, settings, and advanced access."))
        systemCard.addView(UiKit.spacerM(context))
        systemCard.addView(openSystemBtn)
        root.addView(systemCard)

        openOperateBtn.setOnClickListener {
            RoastOperatorPage.show(context, container)
        }

        openReviewBtn.setOnClickListener {
            ReviewHubPage.show(
                context = context,
                container = container,
                onBack = { show(context, container) }
            )
        }

        openStudioBtn.setOnClickListener {
            StudioHubPage.show(
                context = context,
                container = container,
                onBack = { show(context, container) }
            )
        }

        openSystemBtn.setOnClickListener {
            SystemHubPage.show(
                context = context,
                container = container,
                onBack = { show(context, container) }
            )
        }

        scroll.addView(root)
        container.addView(scroll)
    }
}
