package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.UiKit

object SystemHubPage {

    fun show(
        context: Context,
        container: LinearLayout,
        onBack: (() -> Unit)? = null
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "SYSTEM"))
        root.addView(UiKit.pageSubtitle(context, "Profile, app settings, and advanced system access"))
        root.addView(UiKit.spacer(context))

        val navCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "BACK")
        navCard.addView(UiKit.cardTitle(context, "NAVIGATION"))
        navCard.addView(
            UiKit.helperText(
                context,
                "System is the maintenance layer of RoastOS. Use it for profile, settings, and advanced access rather than live roasting flow."
            )
        )
        navCard.addView(UiKit.spacer(context))
        navCard.addView(backBtn)
        root.addView(navCard)
        root.addView(UiKit.spacer(context))

        val profileCard = UiKit.card(context)
        val openProfileBtn = UiKit.primaryButton(context, "OPEN PROFILE")

        profileCard.addView(UiKit.cardTitle(context, "PROFILE"))
        profileCard.addView(
            UiKit.helperText(
                context,
                "Profile pages belong here instead of taking space inside the main roasting workflow."
            )
        )
        profileCard.addView(UiKit.spacer(context))
        profileCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        profileCard.addView(
            UiKit.bodyText(
                context,
                """
Use this area for personal and account-level pages.

This is not part of the main live roasting path.
                """.trimIndent()
            )
        )
        profileCard.addView(UiKit.spacer(context))
        profileCard.addView(openProfileBtn)
        root.addView(profileCard)
        root.addView(UiKit.spacer(context))

        val appSettingsCard = UiKit.card(context)
        val openAppSettingsBtn = UiKit.primaryButton(context, "OPEN APP SETTINGS")

        appSettingsCard.addView(UiKit.cardTitle(context, "APP SETTINGS"))
        appSettingsCard.addView(
            UiKit.helperText(
                context,
                "Application-level configuration and future system settings should live in this layer."
            )
        )
        appSettingsCard.addView(UiKit.spacer(context))
        appSettingsCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        appSettingsCard.addView(
            UiKit.bodyText(
                context,
                """
Use this area for app maintenance, preferences, and future configuration pages.

In the current build, this routes into the profile page as a temporary placeholder.
                """.trimIndent()
            )
        )
        appSettingsCard.addView(UiKit.spacer(context))
        appSettingsCard.addView(openAppSettingsBtn)
        root.addView(appSettingsCard)
        root.addView(UiKit.spacer(context))

        val advancedCard = UiKit.card(context)
        val openAdvancedBtn = UiKit.primaryButton(context, "OPEN ADVANCED / DEBUG")

        advancedCard.addView(UiKit.cardTitle(context, "ADVANCED"))
        advancedCard.addView(
            UiKit.helperText(
                context,
                "Advanced tools, debug pages, and internal diagnostic access belong here, not in the main user workflow."
            )
        )
        advancedCard.addView(UiKit.spacer(context))
        advancedCard.addView(UiKit.sectionLabel(context, "PURPOSE"))
        advancedCard.addView(
            UiKit.bodyText(
                context,
                """
Use this layer for system inspection and future debug-oriented pages.

In the current build, this also routes into the profile page as a safe placeholder.
                """.trimIndent()
            )
        )
        advancedCard.addView(UiKit.spacer(context))
        advancedCard.addView(openAdvancedBtn)
        root.addView(advancedCard)

        openProfileBtn.setOnClickListener {
            ProfilePage.show(context, container)
        }

        openAppSettingsBtn.setOnClickListener {
            ProfilePage.show(context, container)
        }

        openAdvancedBtn.setOnClickListener {
            ProfilePage.show(context, container)
        }

        backBtn.setOnClickListener {
            onBack?.invoke() ?: MainShellPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }
}
