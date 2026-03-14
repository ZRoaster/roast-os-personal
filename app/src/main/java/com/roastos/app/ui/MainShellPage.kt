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
        root.addView(UiKit.pageSubtitle(context, "Operate, review, and build your roasting system"))
        root.addView(UiKit.spacer(context))

        val operateCard = UiKit.card(context)
        val openOperateBtn = UiKit.primaryButton(context, "OPEN OPERATE")
        operateCard.addView(UiKit.cardTitle(context, "OPERATE"))
        operateCard.addView(
            UiKit.helperText(
                context,
                "Live roast control, judgment, and action."
            )
        )
        operateCard.addView(UiKit.spacer(context))
        operateCard.addView(openOperateBtn)
        root.addView(operateCard)
        root.addView(UiKit.spacer(context))

        val reviewCard = UiKit.card(context)
        val openReviewBtn = UiKit.primaryButton(context, "OPEN REVIEW")
        reviewCard.addView(UiKit.cardTitle(context, "REVIEW"))
        reviewCard.addView(
            UiKit.helperText(
                context,
                "Review past batches and compare roast results."
            )
        )
        reviewCard.addView(UiKit.spacer(context))
        reviewCard.addView(openReviewBtn)
        root.addView(reviewCard)
        root.addView(UiKit.spacer(context))

        val studioCard = UiKit.card(context)
        val openStudioBtn = UiKit.primaryButton(context, "OPEN STUDIO")
        studioCard.addView(UiKit.cardTitle(context, "STUDIO"))
        studioCard.addView(
            UiKit.helperText(
                context,
                "Prepare environment, style, and machine setup."
            )
        )
        studioCard.addView(UiKit.spacer(context))
        studioCard.addView(openStudioBtn)
        root.addView(studioCard)
        root.addView(UiKit.spacer(context))

        val systemCard = UiKit.card(context)
        val openSystemBtn = UiKit.primaryButton(context, "OPEN SYSTEM")
        systemCard.addView(UiKit.cardTitle(context, "SYSTEM"))
        systemCard.addView(
            UiKit.helperText(
                context,
                "Profile, settings, and advanced system access."
            )
        )
        systemCard.addView(UiKit.spacer(context))
        systemCard.addView(openSystemBtn)
        root.addView(systemCard)

        openOperateBtn.setOnClickListener {
            RoastOperatorPage.show(context, container)
        }

        openReviewBtn.setOnClickListener {
            ReviewHubPage.show(
                context = context,
                container = container,
                onBack = {
                    show(context, container)
                }
            )
        }

        openStudioBtn.setOnClickListener {
            StudioHubPage.show(
                context = context,
                container = container,
                onBack = {
                    show(context, container)
                }
            )
        }

        openSystemBtn.setOnClickListener {
            SystemHubPage.show(
                context = context,
                container = container,
                onBack = {
                    show(context, container)
                }
            )
        }

        scroll.addView(root)
        container.addView(scroll)
    }
}
