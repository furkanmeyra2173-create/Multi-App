package com.anonh4x.multiapp

import android.os.Bundle
import com.max2idea.android.qube.log.Logger
import com.max2idea.android.qube.main.Config
import com.max2idea.android.qube.main.QubeActivity
import com.max2idea.android.qube.main.QubeApplication

/** QEMU ARM64 system emulation. KVM and host-root access stay disabled. */
class GuestQemuActivity : QubeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        QubeApplication.arch = Config.Arch.arm64
        Config.clientClass = javaClass
        Config.enableKVM = false
        Config.enableEmulatedFloppy = false
        Config.enableMTTCG = QubeApplication.isHost64Bit()
        Config.machineFolder = Config.machineFolder + "other/arm_machines/"
        super.onCreate(savedInstanceState)
        Logger.setupLogFile("/qube/multi-app-guest-log.txt")
    }

    override fun loadQEMULib() {
        System.loadLibrary("qemu-system-aarch64")
    }
}
