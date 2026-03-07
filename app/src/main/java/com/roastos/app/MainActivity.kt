package com.roastos.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "Roast OS\n\n系统启动成功"
        textView.textSize = 24f
        textView.setPadding(48, 96, 48, 48)

        setContentView(textView)
    }
}
