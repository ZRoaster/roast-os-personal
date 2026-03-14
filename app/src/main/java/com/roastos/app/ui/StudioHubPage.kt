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
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.STUDIO
            )
        )
        root.addView(UiKit.spacer(context))

        val preparationCard = UiKit.card(context)
        val openEnvironmentBtn = UiKit.primaryButton(context, "OPEN ENVIRONMENT")
        val openPreparationStudioBtn = UiKit.secondaryButton(context, "OPEN PREPARATION STUDIO")
        preparationCard.addView(UiKit.cardTitle(context, "PREPARATION"))
        preparationCard.addView(UiKit.helperText(context, "Prepare roast context before live operation."))
        preparationCard.addView(UiKit.spacer(context))
        preparationCard.addView(openEnvironmentBtn)
        preparationCard.addView(openPreparationStudioBtn)
        root.addView(preparationCard)
        root.addView(UiKit.spacer(context))

        val styleCard = UiKit.card(context)
        val openRoastStudioBtn = UiKit.primaryButton(context, "OPEN STYLE / STUDIO")
        styleCard.addView(UiKit.cardTitle(context, "STYLE"))
        styleCard.addView(UiKit.helperText(context, "Build style, reuse, and long-term roasting structure."))
        styleCard.addView(UiKit.spacer(context))
        styleCard.addView(openRoastStudioBtn)
        root.addView(styleCard)
        root.addView(UiKit.spacer(context))

        val machineCard = UiKit.card(context)
        val openMachineSetupBtn = UiKit.primaryButton(context, "OPEN MACHINE / CALIBRATION")
        machineCard.addView(UiKit.cardTitle(context, "MACHINE"))
        machineCard.addView(UiKit.helperText(context, "Machine setup, calibration logic, and capability tuning."))
        machineCard.addView(UiKit.spacer(context))
        machineCard.addView(openMachineSetupBtn)
        root.addView(machineCard)
        root.addView(UiKit.spacer(context))

        val intelligenceCard = UiKit.card(context)
        val openIntelligenceStudioBtn = UiKit.primaryButton(context, "OPEN INTELLIGENCE TOOLS")
        intelligenceCard.addView(UiKit.cardTitle(context, "INTELLIGENCE"))
        intelligenceCard.addView(UiKit.helperText(context, "Refine how RoastOS observes, explains, and assists."))
        intelligenceCard.addView(UiKit.spacer(context))
        intelligenceCard.addView(openIntelligenceStudioBtn)
        root.addView(intelligenceCard)
        root.addView(UiKit.spacer(context))

        val navCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "BACK")
        navCard.addView(UiKit.cardTitle(context, "NAVIGATION"))
        navCard.addView(UiKit.helperText(context, "Return to the app home when needed."))
        navCard.addView(UiKit.spacer(context))
        navCard.addView(backBtn)
        root.addView(navCard)

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
