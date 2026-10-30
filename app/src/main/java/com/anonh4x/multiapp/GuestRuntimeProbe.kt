package com.anonh4x.multiapp

import android.content.Context
import java.io.File

/**
 * Probes only Multi-App's private guest-runtime directory.
 *
 * This class intentionally never runs "su", reads /data/adb, or inspects the
 * physical device's root state. Guest root must be supplied by the guest OS.
 */
data class GuestRuntimeSnapshot(
    val engineTitle: String,
    val engineDetails: String,
    val engineStatus: String,
    val guestSystemStatus: String,
    val magiskTitle: String,
    val magiskDetails: String
)

object GuestRuntimeProbe {
    private const val GUEST_DIR = "guest-vm"

    fun inspect(context: Context): GuestRuntimeSnapshot {
        val root = File(context.filesDir, GUEST_DIR)
        val engine = File(root, "engine/qemu-system-aarch64")
        val boot = File(root, "images/boot.img")
        val system = File(root, "images/system.img")
        val vendor = File(root, "images/vendor.img")
        val magiskPayload = File(root, "magisk/magisk.apk")

        val engineReady = engine.isFile && engine.canExecute()
        val imageReady = boot.isFile && system.isFile && vendor.isFile
        val magiskReady = magiskPayload.isFile

        val engineText = if (engineReady) "Bulundu" else "Eksik"
        val guestText = if (imageReady) "Boot / system / vendor imajları bulundu" else "Boot edilebilir konuk imajı yok"
        val engineTitle = if (engineReady && imageReady) "VM dosyaları bulundu" else "Sanal motor henüz bağlı değil"
        val engineDetails = if (engineReady && imageReady) {
            "Dosyalar mevcut. Çalıştırma doğrulaması ayrıca yapılmalıdır."
        } else {
            "QEMU Android çalıştırıcısı ve uyumlu, boot edilebilir ARM64 Android konuk imajı henüz pakete eklenmedi."
        }
        val magiskTitle = if (magiskReady) "Magisk paketi mevcut" else "Magisk konuk kurulumu bekliyor"
        val magiskDetails = when {
            !imageReady -> "Önce sanal Android'in boot etmesi gerekir. Host telefona Magisk kurulmayacak."
            !magiskReady -> "Konuk sistem hazır olduktan sonra Magisk, yalnızca konuk sistemin boot/ramdisk imajına kurulmalıdır."
            else -> "Magisk APK dosyası mevcut; bu tek başına MagiskSU veya magiskd'nin çalıştığını kanıtlamaz."
        }

        return GuestRuntimeSnapshot(
            engineTitle = engineTitle,
            engineDetails = engineDetails,
            engineStatus = engineText,
            guestSystemStatus = guestText,
            magiskTitle = magiskTitle,
            magiskDetails = magiskDetails
        )
    }
}
