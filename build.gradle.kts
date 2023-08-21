plugins {
    kotlin("android") version "1.9.0" apply false
    id("com.android.application") version "8.0.0" apply false
    id("com.android.library") version "8.0.0" apply false
    id("inside") apply false
}

subprojects {
    apply(plugin = "inside")
}
