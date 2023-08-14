plugins {
    kotlin("multiplatform")
    id("org.openjfx.javafxplugin") version "0.0.5"
}

group = "me.zsqw123"
version = "1.0-SNAPSHOT"

configKmmTargets("jvm", "mingwX64", "linuxX64")

kotlin {
    sourceSets {
        mingwX64 {
            binaries {
                executable()
            }
        }
        jvm {
            mainRun {
                mainClass = "MainKt"
            }
        }
    }
}
