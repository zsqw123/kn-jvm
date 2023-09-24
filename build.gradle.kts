plugins {
    id("inside") apply false
    kotlin("android") version "1.9.0" apply false
    id("com.android.application") version "8.0.0" apply false
    id("com.android.library") version "8.0.0" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.11" apply false
    id("org.openjfx.javafxplugin") version "0.0.5" apply false
    kotlin("plugin.serialization") version "1.9.0" apply false
}

subprojects {
    apply(plugin = "inside")
}
