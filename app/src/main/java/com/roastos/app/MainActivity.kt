package com.roastos.app

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Roast OS"
            textSize = 28f
        }

        val subtitle = TextView(this).apply {
            text = "App is running."
            textSize = 18f
        }

        root.addView(title)
        root.addView(subtitle)

        setContentView(root)
    }
}
