plugins {
    kotlin("multiplatform")
    id("insidePublish")
}

configKmmSourceSet(*neededNativePresets, jvmPreset)

commonMainDependencies {
    implementation(D.pb)
}
