#!/usr/bin/env python3
"""Apply a minimal, fail-closed Multi-App brand/package patch to pinned Magisk v31.1."""
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
        return  # idempotent
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

    # The Android namespace stays upstream so the original Magisk source imports
    # and generated resource references remain intact. Only the installed package
    # identity and the native manager/provider identity are changed.
    replace_once(
        root, "app/build-logic/src/main/java/Setup.kt",
        'applicationId = "com.topjohnwu.magisk"',
        f'applicationId = "{NEW_PACKAGE}"'
    )
    replace_once(
        root, "app/core/build.gradle.kts",
        r'buildConfigField("String", "APP_PACKAGE_NAME", "\"com.topjohnwu.magisk\"")',
        f'buildConfigField("String", "APP_PACKAGE_NAME", "\"{NEW_PACKAGE}\"")'
    )
    replace_once(
        root, "native/src/include/consts.rs",
        'pub const APP_PACKAGE_NAME: &str = "com.topjohnwu.magisk";',
        f'pub const APP_PACKAGE_NAME: &str = "{NEW_PACKAGE}";'
    )
    replace_once(
        root, "native/src/include/consts.hpp",
        '#define JAVA_PACKAGE_NAME "com.topjohnwu.magisk"',
        f'#define JAVA_PACKAGE_NAME "{NEW_PACKAGE}"'
    )
    replace_once(
        root, "app/core/src/main/AndroidManifest.xml",
        'android:label="Magisk"',
        'android:label="Multi-App"'
    )

    # Verify the four package identities agree before any native binary is built.
    checks = [
        (root / "app/build-logic/src/main/java/Setup.kt", f'applicationId = "{NEW_PACKAGE}"'),
        (root / "app/core/build.gradle.kts", f'APP_PACKAGE_NAME", "\"{NEW_PACKAGE}\""'),
        (root / "native/src/include/consts.rs", f'APP_PACKAGE_NAME: &str = "{NEW_PACKAGE}"'),
        (root / "native/src/include/consts.hpp", f'JAVA_PACKAGE_NAME "{NEW_PACKAGE}"'),
        (root / "app/core/src/main/AndroidManifest.xml", 'android:label="Multi-App"'),
    ]
    for path, marker in checks:
        if marker not in path.read_text(encoding="utf-8"):
            raise SystemExit(f"Post-patch verification failed: {path.relative_to(root)}")

    print(f"Prepared official Magisk v31.1 ({commit}) as {NEW_PACKAGE} / Multi-App")

if __name__ == "__main__":
    main()
