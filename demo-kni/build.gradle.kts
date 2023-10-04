plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

configKmmSourceSet("jvm", "mingwX64")

val sharedLibraryName = "zsuDemo"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":api"))
            }
        }
    }
    val jvmTarget = jvm {
        mainRun {
            mainClass = "zsu.native.demo.MainKt"
        }
    }
    val jvmCompileTask: Task = jvmTarget.compilations["main"].compileTaskProvider.get()

    mingwX64 {
        binaries.sharedLib(sharedLibraryName) {
            jvmCompileTask.dependsOn(addsNativeLibToJniSources(this))
        }
        configJniInterop(this, "jni", "zsu.jni")
    }
}

dependencies {
    kspCommon(":ksp")
    kspJvm(":ksp")
    kspMingwX64(":ksp")
}

commonMainDependencies {
    implementation(D.pb)
    implementation(project(":api"))
}

ksp {
    arg("kni-jni-package", "zsu.jni")
}

addsKspDependsOn("jvm", "mingwX64")
