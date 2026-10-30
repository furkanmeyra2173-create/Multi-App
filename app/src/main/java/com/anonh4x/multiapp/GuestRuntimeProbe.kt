package com.anonh4x.multiapp

import android.content.Context
import android.os.Build
import java.io.File

data class GuestRuntimeSnapshot(
    val engineTitle: String,
    val engineDetails: String,
    val engineStatus: String,
    val guestSystemStatus: String,
    val magiskTitle: String,
    val magiskDetails: String
)

object GuestRuntimeProbe {
    fun inspect(context: Context): GuestRuntimeSnapshot {
        val lib = File(context.applicationInfo.nativeLibraryDir, "libqemu-system-aarch64.so")
        val arm64 = Build.SUPPORTED_ABIS.any { it == "arm64-v8a" }
        val ready = lib.isFile && arm64
        return GuestRuntimeSnapshot(
            engineTitle = if (ready) "QEMU ARM64 motoru pakette" else "QEMU motoru eksik veya uyumsuz",
            engineDetails = if (ready) "QEMU yerel kütüphanesi bulundu. Rootsuz TCG yazılım emülasyonu kullanılır." else "ARM64 QEMU yerel kütüphanesi bulunamadı. ABI: ${Build.SUPPORTED_ABIS.joinToString()}",
            engineStatus = if (ready) "Yerel kütüphane bulundu" else "Yerel kütüphane bulunamadı",
            guestSystemStatus = "Android 12L (API 32) ARM64 imajı henüz eklenmedi",
            magiskTitle = "Konuk Magisk kurulumu bekliyor",
            magiskDetails = "Önce Android 12L konuk sistemi açılmalı. Magisk yalnızca konuk boot/ramdisk imajına kurulacak; fiziksel telefona dokunulmayacak."
        )
    }
}
