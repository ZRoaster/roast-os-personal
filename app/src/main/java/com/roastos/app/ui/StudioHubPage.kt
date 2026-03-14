package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.UiKit

object StudioHubPage {

    fun show(
        context: Context,
        container: LinearLayout,
        onBack: (() -> Unit)? = null
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "STUDIO"))
        root.addView(UiKit.pageSubtitle(context, "Prepare, build, and refine your roasting system"))
        root.addView(UiKit.spacer(context))

        val navCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "BACK")
        navCard.addView(UiKit.cardTitle(context, "NAVIGATION"))
        navCard.addView(
            UiKit.helperText(
                context,
                "Studio is the preparation and build layer of RoastOS. Use it before roasting or when refining your long-term system."
            )
        )
        navCard.addView(UiKit.spacer(context))
        navCard.addView(backBtn)
        root.addView(navCard)
        root.addView(UiKit.spacer(context))

        val preparationCard = UiKit.card(context)
        val openEnvironmentBtn = UiKit.primaryButton(context, "OPEN ENVIRONMENT")
        val openPreparationStudioBtn = UiKit.secondaryButton(context, "OPEN PREPARATION STUDIO")

        preparationCard.addView(UiKit.cardTitle(context, "PREPARATION"))
        preparationCard.addView(
            UiKit.helperText(
                context,
                "Use this area to prepare roast context before live operation."
            )
        )
        preparationCard.addView(UiKit.spacer(context))
        preparationCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        preparationCard.addView(
            UiKit.bodyText(
                context,
                """
Set environment and preparation information before the roast starts.

This is the first stop when the live screen says you are not ready.
                """.trimIndent()
            )
        )
        preparationCard.addView(UiKit.spacer(context))
        preparationCard.addView(openEnvironmentBtn)
        preparationCard.addView(openPreparationStudioBtn)
        root.addView(preparationCard)
        root.addView(UiKit.spacer(context))

        val styleCard = UiKit.card(context)
        val openRoastStudioBtn = UiKit.primaryButton(context, "OPEN STYLE / STUDIO")

        styleCard.addView(UiKit.cardTitle(context, "STYLE"))
        styleCard.addView(
            UiKit.helperText(
                context,
                "This area is for style building, reuse, and future style-system growth."
            )
        )
        styleCard.addView(UiKit.spacer(context))
        styleCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        styleCard.addView(
            UiKit.bodyText(
                context,
                """
Use this when moving from single-roast thinking to repeatable style thinking.
                """.trimIndent()
            )
        )
        styleCard.addView(UiKit.spacer(context))
        styleCard.addView(openRoastStudioBtn)
        root.addView(styleCard)
        root.addView(UiKit.spacer(context))

        val machineCard = UiKit.card(context)
        val openMachineSetupBtn = UiKit.primaryButton(context, "OPEN MACHINE / CALIBRATION")

        machineCard.addView(UiKit.cardTitle(context, "MACHINE"))
        machineCard.addView(
            UiKit.helperText(
                context,
                "Machine-related setup, calibration logic, and capability tuning belong here."
            )
        )
        machineCard.addView(UiKit.spacer(context))
        machineCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        machineCard.addView(
            UiKit.bodyText(
                context,
                """
Use this when you need to improve machine understanding rather than roast one batch right now.
                """.trimIndent()
            )
        )
        machineCard.addView(UiKit.spacer(context))
        machineCard.addView(openMachineSetupBtn)
        root.addView(machineCard)
        root.addView(UiKit.spacer(context))

        val intelligenceCard = UiKit.card(context)
        val openIntelligenceStudioBtn = UiKit.primaryButton(context, "OPEN INTELLIGENCE TOOLS")

        intelligenceCard.addView(UiKit.cardTitle(context, "INTELLIGENCE"))
        intelligenceCard.addView(
            UiKit.helperText(
                context,
                "AI-related and future advanced intelligence tools can live in this layer."
            )
        )
        intelligenceCard.addView(UiKit.spacer(context))
        intelligenceCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        intelligenceCard.addView(
            UiKit.bodyText(
                context,
                """
Use this when refining how RoastOS observes, explains, and assists.

In the current build, this routes into the main studio workspace.
                """.trimIndent()
            )
        )
        intelligenceCard.addView(UiKit.spacer(context))
        intelligenceCard.addView(openIntelligenceStudioBtn)
        root.addView(intelligenceCard)

        openEnvironmentBtn.setOnClickListener {
            EnvironmentInputPage.show(context, container)
        }

        openPreparationStudioBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        openRoastStudioBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        openMachineSetupBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        openIntelligenceStudioBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        backBtn.setOnClickListener {
            onBack?.invoke() ?: MainShellPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }
}
