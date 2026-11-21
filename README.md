# Previo 🚀
**Automated Runtime Preview Explorer for Compose Multiplatform (CMP)**

Previo is a modern Kotlin Multiplatform library that automatically discovers all `@Preview` annotated composable functions across your modules at compile-time using **KSP (Kotlin Symbol Processing)** with **zero reflection**, and visualizes them in a rich, scroll-safe component catalog explorer across Android, Desktop, iOS, and Web.

---

## ✨ Key Features
- **Zero-Reflection Discovery**: Uses compile-time KSP code generation (`GeneratedPreviewRegistry`) to discover previews.
- **Smart Scroll Management**: Automatically analyzes AST / source code for `LazyColumn`, `LazyRow`, `verticalScroll`, `horizontalScroll`, and `LazyVerticalGrid` to prevent nested scrolling crashes.
- **4-Part Naming Standard**: Organize previews cleanly into `[Group] [Order] [Component] [Details]`.
- **Cross-Platform**: Fully compatible with Android, iOS, Desktop (JVM), and Web (Wasm/JS).
- **Material 3 Design**: Clean, modern M3 interface with vector drawables and bottom navigation.

---

## 📦 Current Version
- **Latest Version**: `1.0.0-alpha02`
- **Hosted on**: [JitPack](https://jitpack.io/#ugurbuga/Previo)

---

## 🛠️ Step-by-Step Integration Guide

### Step 1: Add JitPack Repository
Add JitPack to your repositories (in `settings.gradle.kts` under `dependencyResolutionManagement` or root `build.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add KSP Plugin & Dependencies
In your module's `build.gradle.kts` (e.g. `:shared` or `:app`), apply the KSP plugin and add the Previo runtime and processor dependencies:

```kotlin
plugins {
    // ...
    alias(libs.plugins.ksp) // Ensure KSP plugin is applied
}

dependencies {
    // 1. Previo Runtime Library
    implementation("com.github.ugurbuga.previo:previo-runtime:1.0.0-alpha02")
    
    // 2. Previo KSP Processor (Generates preview registry at compile time)
    // For Multiplatform commonMain metadata:
    add("kspCommonMainMetadata", "com.github.ugurbuga.previo:previo-processor:1.0.0-alpha02")
    
    // OR for Android-only / JVM-only modules:
    // ksp("com.github.ugurbuga.previo:previo-processor:1.0.0-alpha02")
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

### Step 3: Define Previews (Optional Naming Standard)
Annotate your composables with `@Preview` using the 4-part bracket format for automatic categorization:

```kotlin
@Preview(name = "[Buttons] [1] [Primary] [Enabled]")
@Composable
fun PrimaryButtonPreview() {
    Button(onClick = {}) { Text("Primary") }
}
```

### Step 4: Launch PreviewExplorer in Your App
Call `PreviewExplorer` wherever you want to open your component catalog:

```kotlin
import com.ugurbuga.previo.runtime.PreviewExplorer
import com.ugurbuga.previo.runtime.PrevioConfig

@Composable
fun MyCatalogScreen(onBack: () -> Unit) {
    PreviewExplorer(
        onBack = onBack,
        config = PrevioConfig(
            title = "Design System Catalog"
        )
    )
}
```

---

### Running the Multiplatform Apps

- **Android App**: `./gradlew :androidApp:assembleDebug`
- **Desktop App**: `./gradlew :desktopApp:run`
- **Web App (Wasm)**: `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
- **iOS App**: Open `iosApp/iosApp.xcodeproj` in Xcode and run.
