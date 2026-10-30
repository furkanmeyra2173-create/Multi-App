package com.anonh4x.multiapp

import java.io.File
import java.util.concurrent.TimeUnit

data class RootCommandResult(
    val exitCode: Int,
    val output: String,
    val timedOut: Boolean = false
)

data class InstalledModule(
    val id: String,
    val name: String,
    val version: String,
    val state: String
)

data class MagiskSnapshot(
    val isRoot: Boolean,
    val magiskPath: String?,
    val version: String?,
    val daemonPath: String?,
    val modules: List<InstalledModule>,
    val message: String
)

object RootShell {
    fun run(command: String, timeoutSeconds: Long = 30): RootCommandResult {
        return try {
            val process = ProcessBuilder("su", "-c", command)
                .redirectErrorStream(true)
                .start()

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                return RootCommandResult(-1, "Root komutu zaman aşımına uğradı.", true)
            }

            val output = process.inputStream.bufferedReader().use { it.readText() }
            RootCommandResult(process.exitValue(), output.trim())
        } catch (e: Exception) {
            RootCommandResult(127, e.message ?: "su çalıştırılamadı.")
        }
    }

    fun quote(value: String): String = "'" + value.replace("'", "'\\''") + "'"
}

object MagiskControl {
    private const val MODULE_ID_PATTERN = "^[A-Za-z0-9._-]{1,80}$"

    private val inspectScript = """
        uid=$(id -u 2>/dev/null)
        echo "ROOT=$uid"
        MAGISK_BIN=$(command -v magisk 2>/dev/null)
        if [ -z "$MAGISK_BIN" ]; then
          for p in /sbin/.magisk/magisk /sbin/magisk /debug_ramdisk/.magisk/magisk /data/adb/magisk/magisk; do
            if [ -x "$p" ]; then MAGISK_BIN="$p"; break; fi
          done
        fi
        if [ -n "$MAGISK_BIN" ] && [ -x "$MAGISK_BIN" ]; then
          echo "MAGISK=$MAGISK_BIN"
          ver=$( "$MAGISK_BIN" -v 2>/dev/null | head -n 1 )
          [ -z "$ver" ] && ver=$( "$MAGISK_BIN" -c 2>/dev/null | head -n 1 )
          echo "VERSION=$ver"
          echo "DAEMON_PATH=$( "$MAGISK_BIN" --path 2>/dev/null | head -n 1 )"
          for d in /data/adb/modules/*; do
            [ -d "$d" ] || continue
            [ -f "$d/module.prop" ] || continue
            id=${'$'}{d##*/}
            name=$(sed -n 's/^name=//p' "$d/module.prop" | head -n 1 | tr '\t\n' '  ')
            version=$(sed -n 's/^version=//p' "$d/module.prop" | head -n 1 | tr '\t\n' '  ')
            state=Enabled
            [ -f "$d/disable" ] && state=Disabled
            [ -f "$d/remove" ] && state=PendingRemoval
            printf 'MODULE\t%s\t%s\t%s\t%s\n' "$id" "$name" "$version" "$state"
          done
        fi
    """.trimIndent()

    fun inspect(): MagiskSnapshot {
        val result = RootShell.run(inspectScript)
        if (result.timedOut) {
            return MagiskSnapshot(false, null, null, null, emptyList(), result.output)
        }

        val lines = result.output.lines()
        val uid = lines.firstOrNull { it.startsWith("ROOT=") }?.substringAfter("=")
        val root = result.exitCode == 0 && uid == "0"
        val magiskPath = lines.firstOrNull { it.startsWith("MAGISK=") }?.substringAfter("=")?.takeIf { it.isNotBlank() }
        val version = lines.firstOrNull { it.startsWith("VERSION=") }?.substringAfter("=")?.takeIf { it.isNotBlank() }
        val daemonPath = lines.firstOrNull { it.startsWith("DAEMON_PATH=") }?.substringAfter("=")?.takeIf { it.isNotBlank() }
        val modules = lines.mapNotNull { line ->
            if (!line.startsWith("MODULE\t")) return@mapNotNull null
            val parts = line.split('\t')
            if (parts.size < 5) return@mapNotNull null
            InstalledModule(parts[1], parts[2], parts[3], parts[4])
        }
        val message = when {
            !root -> "Root erişimi yok veya Magisk izni verilmedi."
            magiskPath == null -> "Root var; Magisk motoru bulunamadı."
            else -> "Magisk bağlantısı hazır."
        }
        return MagiskSnapshot(root, magiskPath, version, daemonPath, modules, message)
    }

    fun installModule(zipFile: File, magiskPath: String): RootCommandResult {
        if (!zipFile.isFile || !zipFile.name.endsWith(".zip", ignoreCase = true)) {
            return RootCommandResult(2, "Geçerli bir ZIP modülü seçilmedi.")
        }
        val command = "${RootShell.quote(magiskPath)} --install-module ${RootShell.quote(zipFile.absolutePath)}"
        return RootShell.run(command, timeoutSeconds = 180)
    }

    fun setModuleEnabled(id: String, enabled: Boolean): RootCommandResult {
        if (!id.matches(Regex(MODULE_ID_PATTERN))) return RootCommandResult(2, "Geçersiz modül kimliği.")
        val dir = "/data/adb/modules/$id"
        val action = if (enabled) "rm -f ${RootShell.quote("$dir/disable")}" else "touch ${RootShell.quote("$dir/disable")}"
        return RootShell.run("test -d ${RootShell.quote(dir)} && $action")
    }

    fun markModuleForRemoval(id: String): RootCommandResult {
        if (!id.matches(Regex(MODULE_ID_PATTERN))) return RootCommandResult(2, "Geçersiz modül kimliği.")
        val dir = "/data/adb/modules/$id"
        return RootShell.run("test -d ${RootShell.quote(dir)} && touch ${RootShell.quote("$dir/remove")}")
    }
}
