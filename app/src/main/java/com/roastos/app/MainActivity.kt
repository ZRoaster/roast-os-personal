package com.roastos.app

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.roastos.app.ui.DashboardPage
import com.roastos.app.ui.RoastPage

class MainActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL

        val nav = LinearLayout(this)
        nav.orientation = LinearLayout.HORIZONTAL

        val dashBtn = Button(this)
        dashBtn.text = "DASHBOARD"

        val roastBtn = Button(this)
        roastBtn.text = "ROAST"

        val beansBtn = Button(this)
        beansBtn.text = "BEANS"

        val brewBtn = Button(this)
        brewBtn.text = "BREW"

        val aiBtn = Button(this)
        aiBtn
