pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kn-jvm"
includeBuild("insidePlugin")

include(":app")

// dialog just a sample, not actually used for kni project
//include(":demo-dialog")

include(":ksp")
include(":api")

include(":demo-kni")
include(":demo-benchmark")
