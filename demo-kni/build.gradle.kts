plugins {
    kotlin("multiplatform")
    id("org.openjfx.javafxplugin") version "0.0.5"
}

group = "me.zsqw123"
version = "1.0-SNAPSHOT"

configKmmTargets("jvm", "mingwX64")

val sharedLibraryName = "zsuDemo"

kotlin {
    sourceSets {
        val jvmTarget = jvm {
            mainRun {
                mainClass = "zsu.native.demo.MainKt"
            }
        }
        val jvmCompileTask: Task = jvmTarget.compilations["main"].compileTaskProvider.get()

        mingwX64 {
            binaries.sharedLib(sharedLibraryName) {
                linkTask.doLast {
                    copy {
                        from(outputFile)
                        into(jniSourceRoot)
                    }
                }

                jvmCompileTask.dependsOn(linkTask)
            }
            compilations["main"].cinterops.create("jni") {
                val javaHome = File(System.getProperty("java.home"))
                packageName = "zsu.jni"
                includeDirs.allHeadersDirs += files(
                    File(javaHome, "include"),
                    File(javaHome, "include/darwin"),
                    File(javaHome, "include/linux"),
                    File(javaHome, "include/win32")
                )
            }
        }
    }
}
