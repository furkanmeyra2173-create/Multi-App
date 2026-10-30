# Multi-App

Multi-App is being built as an isolated ARM64 Android guest VM. The physical Android installation must remain unrooted.

## Selected guest Android

The first guest target is **Android 12L (API 32), ARM64**. The VM engine is independent from the guest Android version; Android 12L is the first ROM target, not an image bundled in this milestone.

## VM engine

- QEMU ARM64 system emulation is integrated from QubeVM as a pinned Git submodule.
- The intended stock-device path uses TCG software emulation (no KVM and no host root).
- User-mode SLIRP networking is used; TAP networking is not enabled.
- The app opens the QEMU ARM64 machine manager through its own activity.
- QEMU native libraries are built for Android arm64-v8a during GitHub Actions.

QubeVM upstream: https://github.com/rhn-1k/QubeVM (GPL-3.0). See THIRD_PARTY_NOTICES.md.

## Current milestone status

- [x] Add QEMU ARM64 system-emulation source/runtime integration.
- [x] Keep host-root probing and host Magisk operations out of the app.
- [ ] Build and boot a compatible Android 12L ARM64 guest image (kernel, boot/initrd, system and vendor).
- [ ] Verify Android guest boot on a physical non-rooted Android host.
- [ ] Patch Magisk into the guest boot/ramdisk and verify su inside the guest.
- [ ] Add a guest-only ADB/shell and Magisk module manager.

The presence of QEMU binaries does not mean an Android guest has booted. Guest boot and guest root must be demonstrated separately before those controls are enabled.
