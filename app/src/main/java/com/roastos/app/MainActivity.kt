package com.roastos.app

import android.content.Context
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

        val context: Context = this

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val navBar = LinearLayout(context)
        navBar.orientation = LinearLayout.HORIZONTAL

        val dashboardBtn = Button(context)
        dashboardBtn.text = "Dashboard"

        val plannerBtn = Button(context)
        plannerBtn.text = "Planner"

        val roastBtn = Button(context)
        roastBtn.text = "Roast"

        val correctionBtn = Button(context)
        correctionBtn.text = "Correction"

        container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL

        navBar.addView(dashboardBtn)
        navBar.addView(plannerBtn)
        navBar.addView(roastBtn)
        navBar.addView(correctionBtn)

        root.addView(navBar)
        root.addView(container)

        setContentView(root)

        dashboardBtn.setOnClickListener {
            DashboardPage.show(context, container)
        }

        plannerBtn.setOnClickListener {
            PlannerPage.show(context, container)
        }

        roastBtn.setOnClickListener {
            RoastPage.show(context, container)
        }

        correctionBtn.setOnClickListener {
            CorrectionPage.show(context, container)
        }

        DashboardPage.show(context, container)
    }
}
