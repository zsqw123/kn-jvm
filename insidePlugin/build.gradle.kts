plugins {
    kotlin("jvm") version "1.9.0"
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins {
        create("inside") {
            id = "inside"
            implementationClass = "stub.inside.InsidePlugin"
        }
    }
}
