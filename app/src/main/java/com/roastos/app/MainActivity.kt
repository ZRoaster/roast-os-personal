package com.roastos.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.roastos.app.ui.CorrectionPage
import com.roastos.app.ui.DashboardPage
import com.roastos.app.ui.PlannerPage
import com.roastos.app.ui.RoastPage

class MainActivity : AppCompatActivity() {

    private lateinit var pageContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx: android.content.Context = this@MainActivity

        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL

        val navBar = LinearLayout(ctx)
        navBar.orientation = LinearLayout.HORIZONTAL

        val dashboardBtn = Button(ctx)
        dashboardBtn.text = "Dashboard"

        val plannerBtn = Button(ctx)
        plannerBtn.text = "Planner"

        val roastBtn = Button(ctx)
        roastBtn.text = "Roast"

        val correctionBtn = Button(ctx)
        correctionBtn.text = "Correction"

        pageContainer = LinearLayout(ctx)
        pageContainer.orientation = LinearLayout.VERTICAL

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
