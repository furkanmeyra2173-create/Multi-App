# Third-party notices

## Magisk

- Project: Magisk
- Author/upstream: topjohnwu
- Source repository: https://github.com/topjohnwu/Magisk
- Integrated source version: v31.1
- Pinned source commit: 0972f67d97f9b6302b67c5171283330c74114b4a
- License: GNU General Public License version 3 or, at the copyright holder's option, any later version (GPL-3.0-or-later)
- Official license text is also copied to third_party/Magisk-LICENSE.

The APK's Magisk code and native components are compiled from the pinned upstream source during GitHub Actions. The upstream source is not represented as original Multi-App code. All Magisk copyright and license notices remain applicable. The build is not endorsed by or affiliated with the Magisk project maintainer.

Local changes: the installed Android application ID and the Magisk manager/provider package identity are changed to com.anonh4x.multiapp; the launcher label is changed to Multi-App. The exact patch is in tools/prepare_magisk.py.

## QubeVM / QEMU

- Project: QubeVM
- Upstream: https://github.com/rhn-1k/QubeVM
- Pinned commit: b56a4c158d7d8fdaba395bddae2cc95732b85cd9
- License: GNU General Public License version 3.0 (GPL-3.0)
- Source is included as the Git submodule engine/QubeVM; its license text is engine/QubeVM/COPYING.
- QubeVM includes QEMU and supporting libraries. Their individual license notices remain applicable and are retained in the upstream source tree.

Multi-App is not affiliated with or endorsed by the QubeVM or QEMU maintainers.
