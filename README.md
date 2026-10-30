# Multi-App

Multi-App is being rebuilt around an isolated Android guest environment. The physical Android installation must remain unrooted.

## Architecture status

The current branch removes the previous host-root control path. It no longer invokes `su`, reads the host's `/data/adb`, or attempts to install Magisk modules into the physical phone.

A real guest-root design requires all of the following:

1. A VM runtime that can execute an Android guest on an ordinary, non-rooted Android host.
2. A bootable ARM64 Android guest image (kernel/boot, system, vendor and writable data).
3. A guest-only Magisk installation patched into that guest's boot/ramdisk and started by the guest init process.
4. A guest bridge for ADB/shell, file transfer, app installation and Magisk module management. Every command must be routed to the guest, never the host.

## Why the old Magisk APK build was not enough

The Magisk APK is not a virtual Android system and cannot root itself. Magisk's supported deployment installs into a boot image/ramdisk. The previous `MagiskControl.kt` instead ran `su -c` against the host Android system and accessed host `/data/adb/modules`; that did not meet the product requirement and has been removed on this branch.

## Runtime feasibility findings

- Android Virtualization Framework (AVF) APIs are system APIs and require the restricted `MANAGE_VIRTUAL_MACHINE` permission; ordinary third-party apps cannot simply call them on stock consumer devices.
- Twoyi is a useful historical Android-container reference, but its own archived project documents host support only through Android 12, an Android 8.1 guest, and a discontinued upstream app. It is not a drop-in Android 10–16 / current HyperOS solution, and its container root must not be mislabeled as a full Magisk boot-image installation.
- Therefore this branch reports the VM runtime and guest image as missing rather than showing false “rooted” status.

## Current implementation

- Host-root probing and host Magisk module operations have been removed.
- `GuestRuntimeProbe` inspects only Multi-App's private `files/guest-vm` directory.
- The UI explicitly distinguishes the host device from the guest Android and does not claim the VM or Magisk is operational.

## Next engineering milestone

Integrate and test a rootless ARM64 guest VM runtime, produce a bootable Android guest image, then patch Magisk into that guest image. Until those artifacts and the guest boot test exist, VM start and Magisk controls must remain unavailable.

## References

- Android Virtualization Framework API: https://android.googlesource.com/platform/packages/modules/Virtualization/+/HEAD/libs/framework-virtualization/README.md
- Magisk deployment documentation: https://magisk.readthedocs.io/en/latest/developers/deployment.html
- Twoyi source (archived): https://github.com/twoyi/twoyi
