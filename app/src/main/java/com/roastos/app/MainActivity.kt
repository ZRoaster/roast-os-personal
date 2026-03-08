package com.roastos.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import com.roastos.app.ui.CorrectionPage
import com.roastos.app.ui.DashboardPage
import com.roastos.app.ui.PlannerPage
import com.roastos.app.ui.RoastPage

class MainActivity : ComponentActivity() {

    private lateinit var pageContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        val navBar = LinearLayout(this)
        navBar.orientation = LinearLayout.HORIZONTAL

        val dashboardBtn = Button(this)
        dashboardBtn.text = "Dashboard"

        val plannerBtn = Button(this)
        plannerBtn.text = "Planner"

        val roastBtn = Button(this)
        roastBtn.text = "Roast"

        val correctionBtn = Button(this)
        correctionBtn.text = "Correction"

        pageContainer = LinearLayout(this)
        pageContainer.orientation = LinearLayout.VERTICAL

        navBar.addView(dashboardBtn)
        navBar.addView(plannerBtn)
        navBar.addView(roastBtn)
        navBar.addView(correctionBtn)

        root.addView(navBar)
        root.addView(pageContainer)

        setContentView(root)

        dashboardBtn.setOnClickListener {
            DashboardPage.show(this, pageContainer)
        }

        plannerBtn.setOnClickListener {
            PlannerPage.show(this, pageContainer)
        }

        roastBtn.setOnClickListener {
            RoastPage.show(this, pageContainer)
        }

        correctionBtn.setOnClickListener {
            CorrectionPage.show(this, pageContainer)
        }

        DashboardPage.show(this, pageContainer)
    }
}
