package com.roastos.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import com.roastos.app.ui.CorrectionPage
import com.roastos.app.ui.DashboardPage
import com.roastos.app.ui.PlannerPage
import com.roastos.app.ui.RoastPage

class MainActivity : Activity() {

    private lateinit var pageContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx: Context = this

        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(16, 16, 16, 16)

        val navBar = LinearLayout(ctx)
        navBar.orientation = LinearLayout.HORIZONTAL
        navBar.setPadding(0, 0, 0, 16)

        val navParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        val dashboardBtn = Button(ctx)
        dashboardBtn.text = "Dashboard"
        dashboardBtn.layoutParams = navParams

        val plannerBtn = Button(ctx)
        plannerBtn.text = "Planner"
        plannerBtn.layoutParams = navParams

        val roastBtn = Button(ctx)
        roastBtn.text = "Roast"
        roastBtn.layoutParams = navParams

        val correctionBtn = Button(ctx)
        correctionBtn.text = "Correction"
        correctionBtn.layoutParams = navParams

        pageContainer = LinearLayout(ctx)
        pageContainer.orientation = LinearLayout.VERTICAL
        pageContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        navBar.addView(dashboardBtn)
        navBar.addView(plannerBtn)
        navBar.addView(roastBtn)
        navBar.addView(correctionBtn)

        root.addView(navBar)
        root.addView(pageContainer)

        setContentView(root)

        dashboardBtn.setOnClickListener {
            DashboardPage.show(ctx, pageContainer)
        }

        plannerBtn.setOnClickListener {
            PlannerPage.show(ctx, pageContainer)
        }

        roastBtn.setOnClickListener {
            RoastPage.show(ctx, pageContainer)
        }

        correctionBtn.setOnClickListener {
            CorrectionPage.show(ctx, pageContainer)
        }

        DashboardPage.show(ctx, pageContainer)
    }
}
