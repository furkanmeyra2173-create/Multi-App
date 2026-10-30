# Multi-App

**Multi-App with the genuine Magisk v31.1 engine and manager**, built from the official Magisk source and branded with the Android package name `com.anonh4x.multiapp`.

## Magisk integration

The GitHub Actions release build checks out the exact official Magisk v31.1 source commit `0972f67d97f9b6302b67c5171283330c74114b4a`, including all upstream Git submodules, then applies a small, fail-closed identity patch:

- Android application ID: `com.anonh4x.multiapp`
- Magisk manager/provider identity in the Android core and native Rust/C++ code: `com.anonh4x.multiapp`
- Launcher label: `Multi-App`
- Upstream Magisk Compose manager, native MagiskSU, MagiskBoot, MagiskInit, MagiskPolicy, resetprop and module framework are built by the official upstream build script and packaged into the same APK.

This is not a WebView mockup, a copied icon, or a shell-only imitation. The distributable APK is built from Magisk's source. It contains the official manager and ABI-specific native components produced by that source build.

## Android compatibility

The target range is Android 10–16. Upstream Magisk v31.1 has a minimum Android version of 7.0, so this requested range is inside its stated baseline. Device firmware, boot layout, kernel, bootloader state and manufacturer-specific restrictions can still affect whether Magisk can be installed on a particular phone.

## Important installation limits

Installing the Multi-App APK does **not** root a phone by itself. First-time Magisk installation still requires an unlockable/unlocked bootloader and a boot image taken from the **same device and firmware build**. The integrated Magisk Install flow patches the appropriate boot, init_boot, vendor_boot or recovery image. Flashing the patched image is a separate device-specific step. Never flash an image patched from another device. Some installation paths can wipe user data.

Magisk v31.1 changed superuser management: app permissions are configured explicitly in the Magisk Superuser section rather than relying on the old reactive permission prompt. Do not grant root access to apps you do not trust.

## Build from an Android phone

1. Open the repository's **Actions** tab.
2. Select **Build Multi-App with Magisk Engine**.
3. Open the successful run and download the `Multi-App-Magisk-v31.1` artifact.
4. Extract the ZIP and install `app-release.apk`.

The workflow uses the official Magisk build script/toolchain and builds ARM32, ARM64, x86 and x86_64 native outputs.

## Upstream source and license

Magisk is authored by topjohnwu and is licensed under **GNU GPL v3 or later**. The upstream source, exact pinned revision, build script and our small branding patch are publicly available:

- Upstream: https://github.com/topjohnwu/Magisk
- Pinned source: https://github.com/topjohnwu/Magisk/tree/0972f67d97f9b6302b67c5171283330c74114b4a
- Official license: https://github.com/topjohnwu/Magisk/blob/v31.1/LICENSE
- Local patch: `tools/prepare_magisk.py`

See `THIRD_PARTY_NOTICES.md` and `third_party/Magisk-LICENSE` for attribution and license details.

## Multi-account app cloning

The genuine Magisk manager and engine are the current integration milestone. The separate app-cloning / isolated-account runtime is still a later feature; it is not claimed to be implemented by this Magisk build.
