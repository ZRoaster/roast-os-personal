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
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.SYSTEM
            )
        )
        root.addView(UiKit.spacer(context))

        val profileCard = UiKit.card(context)
        val openProfileBtn = UiKit.primaryButton(context, "OPEN PROFILE")

        profileCard.addView(UiKit.cardTitle(context, "PROFILE"))
        profileCard.addView(
            UiKit.helperText(
                context,
                "Personal and account-level pages."
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
                "Application-level preferences and future system settings."
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
                "Advanced tools and future debug-oriented pages."
            )
        )
        advancedCard.addView(UiKit.spacer(context))
        advancedCard.addView(openAdvancedBtn)
        root.addView(advancedCard)
        root.addView(UiKit.spacer(context))

        val navCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "BACK")
        navCard.addView(UiKit.cardTitle(context, "NAVIGATION"))
        navCard.addView(
            UiKit.helperText(
                context,
                "Return to the app home when needed."
            )
        )
        navCard.addView(UiKit.spacer(context))
        navCard.addView(backBtn)
        root.addView(navCard)

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
