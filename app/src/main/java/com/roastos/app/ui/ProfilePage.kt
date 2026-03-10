package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.UiKit

object ProfilePage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {

        container.removeAllViews()

        val scroll = ScrollView(context)

        val root = UiKit.pageRoot(context)

        root.addView(
            UiKit.pageTitle(
                context,
                "PROFILE"
            )
        )

        root.addView(
            UiKit.pageSubtitle(
                context,
                "RoastOS system profile and environment"
            )
        )

        root.addView(UiKit.spacer(context))

        val systemCard = UiKit.card(context)

        systemCard.addView(
            UiKit.cardTitle(
                context,
                "SYSTEM"
            )
        )

        systemCard.addView(
            UiKit.bodyText(
                context,
                """
RoastOS Alpha

Machine Profile
HB M2SE

Simulation
MachineBridge

Session Engine
Running

Insight Engine
Active
                """.trimIndent()
            )
        )

        root.addView(systemCard)

        root.addView(UiKit.spacer(context))

        val environmentCard = UiKit.card(context)

        environmentCard.addView(
            UiKit.cardTitle(
                context,
                "ENVIRONMENT"
            )
        )

        environmentCard.addView(
            UiKit.bodyText(
                context,
                """
Device
Android

UI System
RoastOS UiKit

Build Mode
Debug

Runtime
Local Simulation
                """.trimIndent()
            )
        )

        root.addView(environmentCard)

        scroll.addView(root)

        container.addView(scroll)
    }
}
