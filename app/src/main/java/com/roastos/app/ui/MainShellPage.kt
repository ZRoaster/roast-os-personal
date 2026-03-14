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
                "Live operating layer. Check connection, readiness, current status, judgment, and action."
            )
        )
        operateCard.addView(UiKit.spacer(context))
        operateCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        operateCard.addView(
            UiKit.bodyText(
                context,
                """
Use this when you are about to roast or already roasting.

This is the main live workspace.
                """.trimIndent()
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
                "Review past batches, inspect one roast, and compare two roasts for learning and reuse."
            )
        )
        reviewCard.addView(UiKit.spacer(context))
        reviewCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        reviewCard.addView(
            UiKit.bodyText(
                context,
                """
Use this after roasting.

Start from recent roasts, then go into detail or compare.
                """.trimIndent()
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
                "Preparation and build layer. Environment, planning, style, and machine-related setup live here."
            )
        )
        studioCard.addView(UiKit.spacer(context))
        studioCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        studioCard.addView(
            UiKit.bodyText(
                context,
                """
Use this before roasting or when refining your system.

This is where RoastOS becomes a long-term tool, not just a live screen.
                """.trimIndent()
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
                "Profile, settings, and future advanced or debug-level system pages belong here."
            )
        )
        systemCard.addView(UiKit.spacer(context))
        systemCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        systemCard.addView(
            UiKit.bodyText(
                context,
                """
Use this for app-level maintenance, not for the main roasting workflow.
                """.trimIndent()
            )
        )
        systemCard.addView(UiKit.spacer(context))
        systemCard.addView(openSystemBtn)
        root.addView(systemCard)

        openOperateBtn.setOnClickListener {
            RoastOperatorPage.show(context, container)
        }

        openReviewBtn.setOnClickListener {
            RecentRoastListPage.show(
                context = context,
                container = container,
                onBack = {
                    show(context, container)
                }
            )
        }

        openStudioBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        openSystemBtn.setOnClickListener {
            ProfilePage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }
}
