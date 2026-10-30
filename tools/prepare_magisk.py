#!/usr/bin/env python3
"""Apply a minimal, fail-closed identity patch to the pinned Magisk v31.1 source."""
from pathlib import Path
import subprocess
import sys

EXPECTED_COMMIT = "0972f67d97f9b6302b67c5171283330c74114b4a"
OLD_PACKAGE = "com.topjohnwu.magisk"
NEW_PACKAGE = "com.anonh4x.multiapp"

def replace_once(root: Path, relative: str, old: str, new: str) -> None:
    path = root / relative
    if not path.is_file():
        raise SystemExit(f"Expected upstream file missing: {relative}")
    data = path.read_text(encoding="utf-8")
    old_count = data.count(old)
    new_count = data.count(new)
    if old_count == 0 and new_count == 1:
        return
    if old_count != 1 or new_count != 0:
        raise SystemExit(
            f"Refusing to patch {relative}: expected exactly one old marker, "
            f"found old={old_count}, new={new_count}"
        )
    path.write_text(data.replace(old, new, 1), encoding="utf-8")

def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("Usage: prepare_magisk.py <path-to-Magisk-source>")
    root = Path(sys.argv[1]).resolve()
    try:
        commit = subprocess.check_output(
            ["git", "rev-parse", "HEAD"], cwd=root, text=True
        ).strip()
    except Exception as exc:
        raise SystemExit(f"Cannot verify Magisk source checkout: {exc}")
    if commit != EXPECTED_COMMIT:
        raise SystemExit(
            f"Unexpected Magisk source revision: {commit}; expected {EXPECTED_COMMIT}"
        )

    # Keep upstream Kotlin namespaces unchanged. Change only the installed app
    # identity and the native manager/provider identity used by Magisk's daemon.
    escaped_quote = chr(92) + '"'
    core_package_line = (
        'buildConfigField("String", "APP_PACKAGE_NAME", "'
        + escaped_quote + OLD_PACKAGE + escaped_quote + '")'
    )
    new_core_package_line = (
        'buildConfigField("String", "APP_PACKAGE_NAME", "'
        + escaped_quote + NEW_PACKAGE + escaped_quote + '")'
    )

    replace_once(
        root, "app/build-logic/src/main/java/Setup.kt",
        f'applicationId = "{OLD_PACKAGE}"',
        f'applicationId = "{NEW_PACKAGE}"'
    )
    replace_once(root, "app/core/build.gradle.kts", core_package_line, new_core_package_line)
    replace_once(
        root, "native/src/include/consts.rs",
        f'pub const APP_PACKAGE_NAME: &str = "{OLD_PACKAGE}";',
        f'pub const APP_PACKAGE_NAME: &str = "{NEW_PACKAGE}";'
    )
    replace_once(
        root, "native/src/include/consts.hpp",
        f'#define JAVA_PACKAGE_NAME "{OLD_PACKAGE}"',
        f'#define JAVA_PACKAGE_NAME "{NEW_PACKAGE}"'
    )
    replace_once(
        root, "app/core/src/main/AndroidManifest.xml",
        'android:label="Magisk"',
        'android:label="Multi-App"'
    )

    checks = [
        (root / "app/build-logic/src/main/java/Setup.kt", "applicationId", NEW_PACKAGE),
        (root / "app/core/build.gradle.kts", "APP_PACKAGE_NAME", NEW_PACKAGE),
        (root / "native/src/include/consts.rs", "APP_PACKAGE_NAME", NEW_PACKAGE),
        (root / "native/src/include/consts.hpp", "JAVA_PACKAGE_NAME", NEW_PACKAGE),
        (root / "app/core/src/main/AndroidManifest.xml", "android:label", "Multi-App"),
    ]
    for path, marker, expected in checks:
        data = path.read_text(encoding="utf-8")
        if marker not in data or expected not in data:
            raise SystemExit(f"Post-patch verification failed: {path.relative_to(root)}")

    print(f"Prepared official Magisk v31.1 ({commit}) as {NEW_PACKAGE} / Multi-App")

if __name__ == "__main__":
    main()
