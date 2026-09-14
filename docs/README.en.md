# Moxie — Chinese & English Dictation Practice Apps

> Offline-friendly dictation ("默写") practice tools for middle-school students,
> designed for **tablets / large screens**.
> Two single-file web apps + a dependency-free Android WebView shell.
> **No install, no build, no server — just open the HTML.**

[中文 README](../README.md)

---

## What it is

| App | Description |
|---|---|
| **yuwen-moxie** (Chinese) | Classical poetry & vocabulary dictation, landscape-optimized glassmorphism UI, haptic page feedback, online reading |
| **english-moxie** (English) | Per-unit word bank import (`word,meaning` one per line), randomized question order, phrase translation, localStorage persistence |

Motivation: a student needs to practise dictation at night, the only screen at home is a
tablet, and installing an app is friction. So the whole tool became **one HTML file** —
put it on the home screen, send it over WeChat, open it in any browser.

## Quick start

```bash
# Web — zero install
open web/yuwen-moxie/index.html
open web/english-moxie/index.html

# Android APK — grab a signed build from Releases
```

## Building the APK (no Gradle, no Android Studio)

```bash
# Debian/Ubuntu
sudo apt install openjdk-17-jdk-headless android-sdk-build-tools android-sdk-platform-23
bash scripts/build-apk.sh both      # or: yuwen | english
# output -> dist/moxie-yuwen.apk, dist/moxie-english.apk
```

Toolchain: `javac → d8/dx → aapt → zipalign → apksigner`.
GitHub Actions runs the same script — see `.github/workflows/build-apk.yml`.

## Roadmap

- Externalizable word banks (JSON / CSV import)
- Mistake book + spaced repetition review queue
- Audio dictation mode (TTS reads, student writes blind)
- Export results to PDF for home-school printing
- Community-maintained word banks per textbook edition
- i18n (English UI) & PWA offline install

## Contributing

Contributions of every size are welcome — fixing a typo, adding one unit of vocabulary,
improving docs, reporting a bug. Start with [CONTRIBUTING.md](../CONTRIBUTING.md)
(it includes a full "first pull request" walkthrough, and a web-only path with zero CLI).

Good first tasks: [`good first issue`](https://github.com/zsssal1012-design/moxie/labels/good%20first%20issue).

## License & content notice

- **Code**: Apache License 2.0
- **Bundled word banks**: classical Chinese poems are public domain, but textbook unit
  arrangements and modern prose excerpts may carry publisher rights. This repository does
  **not** grant redistribution rights over bundled content — see
  [content-license.md](content-license.md). For commercial use, supply your own word banks.