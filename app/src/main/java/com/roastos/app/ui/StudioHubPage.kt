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
        root.addView(UiKit.pageSubtitle(context, "Prepare, configure, and build your roasting workflow"))
        root.addView(UiKit.spacerS(context))
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.STUDIO
            )
        )
        root.addView(UiKit.spacer(context))

        val overviewCard = UiKit.card(context)
        overviewCard.addView(UiKit.cardTitle(context, "WORKSPACE OVERVIEW"))
        overviewCard.addView(UiKit.spacerS(context))
        overviewCard.addView(
            UiKit.bodyText(
                context,
                """
Preparation
Environment and pre-roast setup

Style
Reusable direction and roast identity

Machine
Calibration and machine capability

Intelligence
Assistive tools and future smart workflow
                """.trimIndent()
            )
        )
        root.addView(overviewCard)
        root.addView(UiKit.spacer(context))

        val preparationCard = UiKit.card(context)
        val openEnvironmentBtn = UiKit.primaryButton(context, "Environment")
        val openPreparationStudioBtn = UiKit.secondaryButton(context, "Preparation Studio")

        preparationCard.addView(UiKit.cardTitle(context, "PREPARATION"))
        preparationCard.addView(UiKit.helperText(context, "Get the next roast ready before live operation."))
        preparationCard.addView(UiKit.spacerS(context))
        preparationCard.addView(
            UiKit.bodyText(
                context,
                """
Use this area to enter environment context and complete the minimum pre-roast setup.
                """.trimIndent()
            )
        )
        preparationCard.addView(UiKit.spacerM(context))
        preparationCard.addView(openEnvironmentBtn)
        preparationCard.addView(UiKit.spacerS(context))
        preparationCard.addView(openPreparationStudioBtn)
        root.addView(preparationCard)
        root.addView(UiKit.spacer(context))

        val styleCard = UiKit.card(context)
        val openStyleStudioBtn = UiKit.primaryButton(context, "Style Studio")

        styleCard.addView(UiKit.cardTitle(context, "STYLE"))
        styleCard.addView(UiKit.helperText(context, "Build reusable roast direction and long-term style structure."))
        styleCard.addView(UiKit.spacerS(context))
        styleCard.addView(
            UiKit.bodyText(
                context,
                """
Use this area when you want consistency, reuse, and a clearer style language across batches.
                """.trimIndent()
            )
        )
        styleCard.addView(UiKit.spacerM(context))
        styleCard.addView(openStyleStudioBtn)
        root.addView(styleCard)
        root.addView(UiKit.spacer(context))

        val machineCard = UiKit.card(context)
        val openMachineSetupBtn = UiKit.primaryButton(context, "Machine / Calibration")

        machineCard.addView(UiKit.cardTitle(context, "MACHINE"))
        machineCard.addView(UiKit.helperText(context, "Improve machine understanding, response, and calibration logic."))
        machineCard.addView(UiKit.spacerS(context))
        machineCard.addView(
            UiKit.bodyText(
                context,
                """
Use this area for machine-side refinement instead of batch-side review.
                """.trimIndent()
            )
        )
        machineCard.addView(UiKit.spacerM(context))
        machineCard.addView(openMachineSetupBtn)
        root.addView(machineCard)
        root.addView(UiKit.spacer(context))

        val intelligenceCard = UiKit.card(context)
        val openIntelligenceStudioBtn = UiKit.primaryButton(context, "Intelligence Tools")

        intelligenceCard.addView(UiKit.cardTitle(context, "INTELLIGENCE"))
        intelligenceCard.addView(UiKit.helperText(context, "Refine assistive logic, smart tools, and future AI workflow."))
        intelligenceCard.addView(UiKit.spacerS(context))
        intelligenceCard.addView(
            UiKit.bodyText(
                context,
                """
Use this area for advanced helper tools rather than immediate roast execution.
                """.trimIndent()
            )
        )
        intelligenceCard.addView(UiKit.spacerM(context))
        intelligenceCard.addView(openIntelligenceStudioBtn)
        root.addView(intelligenceCard)
        root.addView(UiKit.spacer(context))

        val accessCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "Home")
        accessCard.addView(UiKit.cardTitle(context, "MORE ACCESS"))
        accessCard.addView(UiKit.helperText(context, "Return to the main shell when needed."))
        accessCard.addView(UiKit.spacerM(context))
        accessCard.addView(backBtn)
        root.addView(accessCard)

        openEnvironmentBtn.setOnClickListener {
            EnvironmentInputPage.show(context, container)
        }

        openPreparationStudioBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        openStyleStudioBtn.setOnClickListener {
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
