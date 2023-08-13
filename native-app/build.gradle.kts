plugins {
    kotlin("multiplatform")
    id("inside")
}

group = "me.zsqw123"
version = "1.0-SNAPSHOT"

configKmmTargets("jvm", "mingwX64", "linuxX64")
