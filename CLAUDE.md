# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Screenshot to Clipboard** is a Minecraft client mod that copies screenshot image data to the system clipboard when a screenshot is taken. Supports Windows/Linux (via AWT) and macOS (via Objective-C bridge). Currently targets Minecraft 1.20.6+ on the `1.20-arch` branch.

## Build Commands

```bash
./gradlew build                    # Build all subprojects (common, fabric, neoforge)
./gradlew :fabric:build            # Build Fabric mod only
./gradlew :neoforge:build          # Build NeoForge mod only
./gradlew publish                  # Build + publish to Modrinth/CurseForge/GitHub (requires tokens)
```

Requires JDK 21. Output jars are in `fabric/build/libs/` and `neoforge/build/libs/`.

## Architecture

Uses **Architectury** (with Architectury Loom) for multi-loader development. Three Gradle subprojects:

- **`common/`** — Platform-independent code shared by all loaders. Contains the core clipboard logic (`ScreenshotToClipboard.java`, `MacOSCompat.java`) and common mixins (`AWTHackMixin`, `NativeImagePointerAccessor`).
- **`fabric/`** — Fabric loader entrypoint and mixins. Uses Mixin injection into `ScreenshotRecorder.method_1661` (the lambda that saves the screenshot file). Also has Fabrishot mod compatibility.
- **`neoforge/`** — NeoForge loader entrypoint. Uses NeoForge's `ScreenshotEvent` instead of mixins for screenshot capture.

### Key patterns

- **Mappings**: Uses Yarn mappings with an Architectury NeoForge patch layer (`yarn-mappings-patch-neoforge`). All code uses Yarn-named intermediaries (e.g., `ScreenshotRecorder`, `NativeImage`).
- **Screenshot interception**: Fabric uses `@Mixin` on `ScreenshotRecorder` targeting `method_1661` (the screenshot-saving lambda). NeoForge uses its native `ScreenshotEvent`.
- **Platform clipboard**: Windows/Linux uses AWT (`java.awt.datatransfer.Clipboard`). macOS uses Objective-C bridge (`ca.weblite:java-objc-bridge`) since AWT and GLFW conflict on macOS.
- **AWT headless hack**: `AWTHackMixin` injects at `Main.main` HEAD to set `java.awt.headless=false` on non-macOS systems, before any other mod loads AWT.
- **Shadow plugin**: Common code is shadow-bundled into each loader's jar via `shadowBundle` configuration.
- **Version support metadata**: Each loader's `build.gradle` has `supportedVersions` and `versionSupportName` variables used for Modrinth/CurseForge publishing. These must be updated when adding new MC version support.

### Mixin targets (Fabric)

The Fabric mixins target `method_1661` in `ScreenshotRecorder` — this is Yarn's name for the lambda inside `saveScreenshotInner`/`method_1662` that receives the `NativeImage`, `File`, and `Consumer<Text>`. When updating MC versions, verify this intermediary name hasn't changed.

## Version Configuration

All version numbers are centralized in root `gradle.properties`:
- `minecraft_version`, `yarn_mappings`, `fabric_loader_version`, `fabric_api_version`, `neoforge_version`, `yarn_mappings_patch_neoforge_version`

## Publishing

Release is triggered by creating a GitHub release. The `release.yml` workflow runs `./gradlew publish` which uploads to GitHub Releases, Modrinth, and CurseForge (tokens via GitHub Secrets: `GITHUB_TOKEN`, `MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`).
