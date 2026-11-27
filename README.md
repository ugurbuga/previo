# Previo 🚀
**Automated Runtime Preview Explorer for Compose Multiplatform (CMP)**

Previo is a modern Kotlin Multiplatform library that automatically discovers all `@Preview` annotated composable functions across your modules at compile-time using **KSP (Kotlin Symbol Processing)** with **zero reflection**, visualizes them in a rich component catalog explorer across Android, Desktop, iOS, and Web, and builds an **interactive Composable Hierarchy Call Tree**.

---

## ✨ Key Features
- **Zero-Reflection Discovery**: Uses compile-time KSP code generation (`GeneratedPreviewRegistry`) to discover previews with zero runtime overhead.
- **Interactive Composable Hierarchy Tree**: Parses the nested composable call tree at compile time into `ComposableNode`, rendering an expandable/collapsible hierarchy overlay on detail screens.
- **Smart Scroll Management**: Automatically analyzes AST / source code for `LazyColumn`, `LazyRow`, `verticalScroll`, `horizontalScroll`, and `LazyVerticalGrid` to prevent nested scrolling crashes.
- **Purpose-Named `PrevioColors` Palette**: Categorized color system (`screenBackground`, `cardBackground`, `primaryText`, `secondaryText`, `dividerOrBorder`, `chipSelectedBackground`, `chipSelectedText`, `iconTint`, `badgeBackground`, `badgeText`) with sleek Light (`PrevioDefaults.LightColors`) and Dark (`PrevioDefaults.DarkColors`) presets.
- **Parametric Feature Flags & Config**: Full control over library behavior with `enabled`, `collectPreviews`, `collectHierarchyTree`, `showExtraTreeInfo`, `isDarkTheme`, and custom color palettes.
- **4-Part Naming Standard**: Organize previews cleanly into `[Group] [Order] [Component] [Details]`.
- **Native Interop**: Automatically handles Android system back buttons/gestures (`PrevioBackHandler`) and status bar icon color adaptation (`SetStatusBarColor`).
- **Internationalization (i18n)**: Fully localized in English using Compose Multiplatform `stringResource`.
- **Cross-Platform**: Fully compatible with Android, iOS, Desktop (JVM), and Web (Wasm/JS).

---

## 📦 Current Version
- **Latest Version**: `1.0.0-alpha15`
- **Hosted on**: [Maven Central](https://central.sonatype.com/) & GitHub Packages

---

## 🛠️ Step-by-Step Integration Guide

### Step 1: Add Repositories
Ensure `mavenCentral()` is in your repositories (in `settings.gradle.kts` under `dependencyResolutionManagement` or root `build.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

### Step 2: Configure KSP Plugin

1. **In your version catalog (`gradle/libs.versions.toml`)**:
```toml
[versions]
kotlin = "2.4.21" # Must match your Kotlin version
ksp = "2.3.14"    # KSP version matching your Kotlin version

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

2. **In your root `build.gradle.kts`**:
```kotlin
plugins {
    alias(libs.plugins.ksp) apply false
}
```

3. **In your module's `build.gradle.kts` (e.g. `:shared`)**:
```kotlin
plugins {
    alias(libs.plugins.ksp)
}
```

### Step 3: Add Previo Dependencies
In your module's `build.gradle.kts`:

```kotlin
dependencies {
    // 1. Previo Runtime Library
    implementation("io.github.ugurbuga:previo-runtime:1.0.0-alpha15")
    
    // 2. Previo KSP Processor (Generates preview registry & hierarchy tree at compile time)
    add("kspCommonMainMetadata", "io.github.ugurbuga:previo-processor:1.0.0-alpha15")
}
```

*Note for Kotlin Multiplatform projects using commonMain metadata KSP:*
Ensure KSP generated metadata source directory is linked in your multiplatform `sourceSets`:
```kotlin
kotlin {
    sourceSets {
        named("commonMain").configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
    }
}
```

### Step 4: Define Previews (Optional Naming Standard)
Annotate your composables with `@Preview` using the 4-part bracket format for automatic categorization:

```kotlin
@Preview(name = "[Buttons] [1] [Primary] [Enabled]")
@Composable
fun PrimaryButtonPreview() {
    Button(onClick = {}) { Text("Primary") }
}
```

### Step 5: Launch Previo in Your App

You can open Previo via `PrevioOverlay` or programmatically with `Previo.launch()`:

```kotlin
import com.ugurbuga.previo.runtime.Previo
import com.ugurbuga.previo.runtime.PrevioConfig
import com.ugurbuga.previo.runtime.PrevioColors
import com.ugurbuga.previo.runtime.PrevioDefaults
import com.ugurbuga.previo.runtime.PrevioOverlay

@Composable
fun App() {
    val myConfig = PrevioConfig(
        title = "Design System Catalog",
        enabled = true, // Set to false in release builds
        collectPreviews = true,
        collectHierarchyTree = true,
        isDarkTheme = true,
        colors = PrevioDefaults.DarkColors
    )

    // Option A: Programmatic Trigger
    Button(onClick = { Previo.launch() }) {
        Text("Open Previo Explorer")
    }

    // Option B: Include PrevioOverlay in your root content scope
    PrevioOverlay(config = myConfig) {
        // Your main app screen content
    }
}
```

---

## 🎨 Customizing `PrevioConfig` & `PrevioColors`

Previo provides comprehensive configuration options via `PrevioConfig`:

| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `title` | `String` | `"Previo Explorer"` | Title displayed in the top bar of the explorer. |
| `enabled` | `Boolean` | `true` | Master switch to enable or disable Previo (e.g. set to `false` in release builds). |
| `collectPreviews` | `Boolean` | `true` | Whether to load and display discovered previews. |
| `collectHierarchyTree` | `Boolean` | `true` | Whether to enable the Composable hierarchy call tree inspection feature. |
| `colors` | `PrevioColors?` | `null` | Custom color palette (`PrevioColors`). Defaults to `PrevioDefaults.LightColors` or `DarkColors`. |
| `isDarkTheme` | `Boolean` | `false` | Automatically picks dark default colors if `colors` is null and `isDarkTheme` is true. |
| `showExtraTreeInfo` | `Boolean` | `false` | Displays advanced node debug metadata in the hierarchy tree. |

### Purpose-Named Color Palette (`PrevioColors`)
```kotlin
val customPalette = PrevioColors(
    screenBackground = Color(0xFF0F172A),
    cardBackground = Color(0xFF1E293B),
    primaryText = Color(0xFFF8FAFC),
    secondaryText = Color(0xFF94A3B8),
    dividerOrBorder = Color(0xFF334155),
    chipSelectedBackground = Color(0xFF312E81),
    chipSelectedText = Color(0xFF818CF8),
    iconTint = Color(0xFFCBD5E1),
    badgeBackground = Color(0xFF334155),
    badgeText = Color(0xFFE2E8F0)
)
```

---

### Running Multiplatform Apps

- **Android App**: `./gradlew :androidApp:assembleDebug`
- **Desktop App**: `./gradlew :desktopApp:run`
- **Web App (Wasm)**: `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
- **iOS App**: Open `iosApp/iosApp.xcodeproj` in Xcode and run.
