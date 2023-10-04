plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("insidePublish")
}

dependencies {
    implementation(D.ksp)
    implementation(D.poet)
    implementation(D.poetKsp)
    implementation(D.pb)
    implementation(project(":api"))
    jvmTestDeps()
}

tasks.test {
    useJUnitPlatform()
}
