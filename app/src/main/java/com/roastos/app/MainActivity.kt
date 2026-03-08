package com.roastos.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import com.roastos.app.ui.DashboardPage
import com.roastos.app.ui.PlannerPage
import com.roastos.app.ui.RoastPage
import com.roastos.app.ui.CorrectionPage

class MainActivity : ComponentActivity() {

    private lateinit var container: LinearLayout

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

        container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL

        navBar.addView(dashboardBtn)
        navBar.addView(plannerBtn)
        navBar.addView(roastBtn)
        navBar.addView(correctionBtn)

        root.addView(navBar)
        root.addView(container)

        setContentView(root)

        dashboardBtn.setOnClickListener {
            DashboardPage.show(applicationContext, container)
        }

        plannerBtn.setOnClickListener {
            PlannerPage.show(applicationContext, container)
        }

        roastBtn.setOnClickListener {
            RoastPage.show(applicationContext, container)
        }

        correctionBtn.setOnClickListener {
            CorrectionPage.show(applicationContext, container)
        }

        DashboardPage.show(applicationContext, container)
    }
}
