package com.anonh4x.multiapp

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(0xFF101014.toInt())
        }

        val title = TextView(this).apply {
            text = "MULTI-APP"
            textSize = 30f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Çoklu uygulama alanı ve Magisk yönetimi"
            textSize = 15f
            setTextColor(0xFFBDB8C8.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }
        val status = TextView(this).apply {
            text = "Magisk durumu: Henüz kontrol edilmedi\n\nModül yöneticisi: Hazırlanıyor\n\nUygulama klonlama: Hazırlanıyor"
            textSize = 16f
            setTextColor(0xFFE7E0EC.toInt())
            gravity = Gravity.CENTER
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        setContentView(root)
    }
}
