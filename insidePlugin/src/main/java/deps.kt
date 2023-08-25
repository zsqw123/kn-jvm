import org.gradle.api.Project

@Suppress("ConstPropertyName")
object D {
    const val ksp = "com.google.devtools.ksp:symbol-processing-api:1.9.0-1.0.11"
}

fun Project.jvmTestDeps(configurationName: String = "testImplementation") {
    dependencies.apply {
        add(configurationName, platform("org.junit:junit-bom:5.9.1"))
    }
}
