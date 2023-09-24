plugins {
    kotlin("multiplatform")
}

configKmmSourceSet(*allNativePresets, *allJvmPresets)

commonMainDependencies {
    implementation(D.pb)
}
