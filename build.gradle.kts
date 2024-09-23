plugins {
    id("inside") apply false
    kotlin("android") version "2.0.20" apply false
    id("com.android.application") version "8.0.0" apply false
    id("com.android.library") version "8.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
    id("org.openjfx.javafxplugin") version "0.0.5" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
}

subprojects {
    apply(plugin = "inside")
}
