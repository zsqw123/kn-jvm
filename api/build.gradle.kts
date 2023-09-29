plugins {
    kotlin("multiplatform")
}

configKmmSourceSet(*neededNativePresets, *allJvmPresets)

commonMainDependencies {
    implementation(D.pb)
}
