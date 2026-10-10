// Top-level build file — declares plugin versions once so modules can apply
// them without repeating a version number (avoids version drift).
// AGP 9+ has built-in Kotlin support — the separate
// org.jetbrains.kotlin.android plugin is no longer used (or allowed).
plugins {
    id("com.android.application") version "9.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
}
