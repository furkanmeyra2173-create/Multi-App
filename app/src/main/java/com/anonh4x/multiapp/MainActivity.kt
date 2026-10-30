package com.anonh4x.multiapp

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var page: LinearLayout
    private lateinit var rootStatus: TextView
    private lateinit var versionStatus: TextView
    private lateinit var moduleList: LinearLayout
    private lateinit var refreshButton: MaterialButton
    private lateinit var installButton: MaterialButton
    private var lastSnapshot: MagiskSnapshot? = null

    private val modulePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) prepareModuleInstall(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        refreshMagisk()
    }

    private fun buildScreen() {
        val scroll = ScrollView(this)
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(32))
            setBackgroundColor(Color.rgb(15, 16, 21))
        }
        scroll.addView(page)

        page.addView(label("MULTI-APP", 28f, Color.WHITE, true))
        page.addView(label("Uygulama alanları  •  Magisk yönetimi", 14f, 0xFFB9B8C4.toInt()))
        page.addView(space(18))

        val statusCard = card()
        statusCard.addView(label("ROOT / MAGISK DURUMU", 13f, 0xFFC9B8FF.toInt(), true))
        rootStatus = label("Kontrol ediliyor…", 17f, Color.WHITE, true)
        versionStatus = label("Magisk sürümü: —", 14f, 0xFFCAC7D2.toInt())
        statusCard.addView(rootStatus)
        statusCard.addView(versionStatus)
        refreshButton = actionButton("Durumu yenile") { refreshMagisk() }
        statusCard.addView(refreshButton, matchWrap())
        page.addView(statusCard)

        page.addView(space(14))
        val moduleCard = card()
        moduleCard.addView(label("MAGISK MODÜLLERİ", 17f, Color.WHITE, true))
        moduleCard.addView(label("Yükle, etkinleştir veya sonraki açılışta kaldır.", 13f, 0xFFB9B8C4.toInt()))
        installButton = actionButton("ZIP modülü yükle") {
            modulePicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
        }
        moduleCard.addView(installButton, matchWrap())
        val listButton = actionButton("Modülleri yenile") { refreshMagisk() }
        moduleCard.addView(listButton, matchWrap())
        moduleList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        moduleCard.addView(space(8))
        moduleCard.addView(moduleList)
        page.addView(moduleCard)

        page.addView(space(14))
        val appsCard = card()
        appsCard.addView(label("ÇOKLU UYGULAMA", 17f, Color.WHITE, true))
        appsCard.addView(label("Uygulama klonlama ve ayrı çalışma alanları sonraki geliştirme aşamasında. Android 10–16 hedefleniyor.", 14f, 0xFFC9C6D0.toInt()))
        page.addView(appsCard)

        page.addView(space(16))
        page.addView(label("Not: Modül ZIP dosyaları root yetkisiyle kurulum betikleri çalıştırabilir. Yalnızca güvendiğin kaynaklardan modül yükle.", 12f, 0xFFFFD18A.toInt()))
        setContentView(scroll)
    }

    private fun refreshMagisk() {
        refreshButton.isEnabled = false
        installButton.isEnabled = false
        rootStatus.text = "Root erişimi kontrol ediliyor…"
        Thread {
            val snapshot = MagiskControl.inspect()
            runOnUiThread {
                lastSnapshot = snapshot
                rootStatus.text = snapshot.message
                versionStatus.text = buildString {
                    append("Magisk sürümü: ")
                    append(snapshot.version ?: "bulunamadı")
                    if (snapshot.daemonPath != null) {
                        append("\nMotor yolu: ")
                        append(snapshot.daemonPath)
                    }
                }
                refreshButton.isEnabled = true
                installButton.isEnabled = snapshot.isRoot && snapshot.magiskPath != null
                renderModules(snapshot.modules)
            }
        }.start()
    }

    private fun renderModules(modules: List<InstalledModule>) {
        moduleList.removeAllViews()
        if (modules.isEmpty()) {
            moduleList.addView(label("Modül bulunamadı veya Magisk erişilebilir değil.", 14f, 0xFFC9C6D0.toInt()))
            return
        }

        modules.forEach { module ->
            val row = card()
            row.addView(label(module.name.ifBlank { module.id }, 15f, Color.WHITE, true))
            row.addView(label("${module.id}  •  ${module.version.ifBlank { "sürüm belirtilmemiş" }}  •  ${module.state}", 12f, 0xFFC9C6D0.toInt()))
            val actions = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            if (module.state != "PendingRemoval") {
                val enabled = module.state != "Disabled"
                val toggle = actionButton(if (enabled) "Devre dışı bırak" else "Etkinleştir") {
                    runModuleAction("Modül durumu güncelleniyor…") {
                        MagiskControl.setModuleEnabled(module.id, !enabled)
                    }
                }
                actions.addView(toggle, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(6) })
                val remove = actionButton("Kaldır") {
                    AlertDialog.Builder(this)
                        .setTitle("Modül kaldırılsın mı?")
                        .setMessage("${module.name} sonraki yeniden başlatmada kaldırılmak üzere işaretlenecek.")
                        .setNegativeButton("Vazgeç", null)
                        .setPositiveButton("İşaretle") { _, _ ->
                            runModuleAction("Modül kaldırılmak üzere işaretleniyor…") {
                                MagiskControl.markModuleForRemoval(module.id)
                            }
                        }
                        .show()
                }
                actions.addView(remove, LinearLayout.LayoutParams(0, dp(48), 1f))
            }
            row.addView(actions)
            moduleList.addView(row)
            moduleList.addView(space(8))
        }
    }

    private fun prepareModuleInstall(uri: Uri) {
        val name = queryDisplayName(uri) ?: "module.zip"
        if (!name.endsWith(".zip", ignoreCase = true)) {
            showMessage("Dosya türü", "Magisk modülü ZIP biçiminde olmalıdır.")
            return
        }
        Thread {
            try {
                val target = File(cacheDir, "module-${UUID.randomUUID()}.zip")
                val input = contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Dosya açılamadı.")
                input.use { source ->
                    target.outputStream().use { sink ->
                        val buffer = ByteArray(8192)
                        var total = 0L
                        while (true) {
                            val count = source.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= 500L * 1024L * 1024L) { "Modül ZIP dosyası 500 MB sınırını aşıyor." }
                            sink.write(buffer, 0, count)
                        }
                    }
                }
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("Magisk modülü yüklensin mi?")
                        .setMessage("$name dosyası root yetkisiyle çalıştırılacak. Yalnızca kaynağına güvendiğin ZIP dosyalarını yükle.")
                        .setNegativeButton("İptal") { _, _ -> target.delete() }
                        .setPositiveButton("Yükle") { _, _ -> installModuleFile(target) }
                        .show()
                }
            } catch (e: Exception) {
                runOnUiThread { showMessage("Dosya okunamadı", e.message ?: "Bilinmeyen hata") }
            }
        }.start()
    }

    private fun installModuleFile(file: File) {
        val magiskPath = lastSnapshot?.magiskPath
        if (magiskPath == null) {
            file.delete()
            showMessage("Magisk bulunamadı", "Önce root ve Magisk durumunu yenile.")
            return
        }
        installButton.isEnabled = false
        rootStatus.text = "Modül yükleniyor…"
        Thread {
            val result = MagiskControl.installModule(file, magiskPath)
            file.delete()
            runOnUiThread {
                showMessage(
                    if (result.exitCode == 0) "Kurulum komutu tamamlandı" else "Modül yüklenemedi",
                    result.output.ifBlank { "Çıkış kodu: ${result.exitCode}" }.takeLast(3500)
                )
                refreshMagisk()
            }
        }.start()
    }

    private fun runModuleAction(progress: String, action: () -> RootCommandResult) {
        rootStatus.text = progress
        Thread {
            val result = action()
            runOnUiThread {
                if (result.exitCode != 0) {
                    showMessage("İşlem başarısız", result.output.ifBlank { "Çıkış kodu: ${result.exitCode}" })
                }
                refreshMagisk()
            }
        }.start()
    }

    private fun queryDisplayName(uri: Uri): String? {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) return cursor.getString(index)
            }
        }
        return null
    }

    private fun showMessage(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("Tamam", null).show()
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
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

    private fun label(textValue: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = textValue
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(4), 0, dp(6))
        }

    private fun actionButton(textValue: String, action: () -> Unit): MaterialButton =
        MaterialButton(this).apply {
            text = textValue
            isAllCaps = false
            textSize = 14f
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(0xFF6750A4.toInt())
            setOnClickListener { action() }
            layoutParams = matchWrap()
        }

    private fun matchWrap() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun space(height: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
