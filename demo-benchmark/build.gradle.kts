plugins {
    kotlin("jvm")
    application
}

dependencies {
    testImplementation(platform(D.junitBom))
    testImplementation(D.junitJupiter)
    implementation(project(":demo-kni"))
    implementation(D.coroutine)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.example.PerformanceTestKt")
}
