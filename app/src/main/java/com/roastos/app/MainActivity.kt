package com.roastos.app

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.roastos.app.ui.DashboardPage

class MainActivity : AppCompatActivity() {

    private lateinit var rootContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MachineBridge.start()

        rootContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        setContentView(rootContainer)

        DashboardPage.show(this, rootContainer)
    }

    override fun onDestroy() {
        super.onDestroy()
        MachineBridge.stop()
    }
}
