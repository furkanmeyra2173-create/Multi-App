package com.anonh4x.multiapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var page: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        val snapshot = GuestRuntimeProbe.inspect(this)
        val scroll = ScrollView(this)
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(32))
            setBackgroundColor(Color.rgb(15, 16, 21))
        }
        page.addView(label("MULTI-APP", 28f, Color.WHITE, true))
        page.addView(label("Sanal Android • QEMU ARM64", 15f, 0xFFC9C6D0.toInt()))
        page.addView(space(20))

        val vm = card()
        vm.addView(label("SANAL ANDROID MOTORU", 13f, 0xFFC9B8FF.toInt(), true))
        vm.addView(label(snapshot.engineTitle, 19f, Color.WHITE, true))
        vm.addView(label(snapshot.engineDetails, 14f, 0xFFC9C6D0.toInt()))
        vm.addView(label("Motor: ${snapshot.engineStatus}", 14f, Color.WHITE))
        vm.addView(label("Konuk sistem: ${snapshot.guestSystemStatus}", 14f, Color.WHITE))
        page.addView(vm)

        page.addView(space(14))
        val root = card()
        root.addView(label("MAGISK — KONUK ANDROID", 13f, 0xFFC9B8FF.toInt(), true))
        root.addView(label(snapshot.magiskTitle, 18f, Color.WHITE, true))
        root.addView(label(snapshot.magiskDetails, 14f, 0xFFC9C6D0.toInt()))
        root.addView(label("Host telefon rootsuz kalır.", 13f, 0xFF8FE0B0.toInt(), true))
        page.addView(root)

        page.addView(space(14))
        val device = card()
        device.addView(label("CİHAZ BİLGİSİ", 13f, 0xFFC9B8FF.toInt(), true))
        device.addView(label("Android API: ${Build.VERSION.SDK_INT}", 14f, Color.WHITE))
        device.addView(label("İşlemci ABI: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Bilinmiyor"}", 14f, Color.WHITE))
        page.addView(device)

        page.addView(space(18))
        page.addView(MaterialButton(this).apply {
            text = "QEMU sanal makine ekranını aç"
            isEnabled = snapshot.engineStatus == "Yerel kütüphane bulundu"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, GuestQemuActivity::class.java))
            }
        })
        page.addView(space(12))
        page.addView(label(
            "Motor ARM64 sanal makineleri çalıştırmak için eklendi. Android 12L imajı ve konuk Magisk henüz bu APK'ya dahil değil; motor ekranının açılması Android'in boot ettiği anlamına gelmez.",
            13f,
            0xFFFFD18A.toInt()
        ))
        scroll.addView(page)
        setContentView(scroll)
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = GradientDrawable().apply {
            setColor(0xFF20212A.toInt())
            cornerRadius = dp(18).toFloat()
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(2) }
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(4), 0, dp(7))
        }

    private fun space(height: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
