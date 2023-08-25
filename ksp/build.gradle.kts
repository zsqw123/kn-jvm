plugins {
    kotlin("jvm")
}

group = "org.example"
version = "unspecified"

dependencies {
    implementation(D.ksp)
}

jvmTestDeps()

tasks.test {
    useJUnitPlatform()
}
