plugins {
    kotlin("multiplatform")
}

group = "org.example"
version = "unspecified"

configKmmSourceSet(*allNativePresets, *allJvmPresets)
