import java.util.*

plugins {
    `maven-publish`
    signing
}

val properties = Properties()

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
rootProject.file("publish.properties")
    .takeIf { it.exists() }?.reader()?.use {
        properties.load(it)
    }


ext["signing.keyId"] = properties["signing.keyId"]
ext["signing.password"] = properties["signing.password"]
ext["signing.secretKeyRingFile"] = properties["signing.secretKeyRingFile"]
val mavenCentralUsername: String? by properties
val mavenCentralPassword: String? by properties
val githubEmail: String? by properties
val projectGitUrl: String? by properties
val kniVersion: String by project
val mavenArtifactId: String = property("MAVEN_ARTIFACT") as? String ?: project.name

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = mavenCentralUsername
                password = mavenCentralPassword
            }
        }
    }

    afterEvaluate {
        publications.withType<MavenPublication> {
            groupId = "io.github.zsqw123"
            version = kniVersion
            val artifactName = name
            artifactId = if ("kotlinMultiplatform" == artifactName) {
                mavenArtifactId
            } else {
                "$mavenArtifactId-$artifactName"
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("kn-jvm")
            description.set("Auto JNI binding based on Kotlin Multiplatform")
            url.set(projectGitUrl)

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("zsqw123")
                    name.set("zsub")
                    email.set(githubEmail)
                }
            }
            scm {
                url.set(projectGitUrl)
            }
        }
    }
}

// Signing artifacts. `signing.*` extra properties values will be used
signing {
    val signTasks = sign(publishing.publications)
    afterEvaluate {
        tasks.withType(AbstractPublishToMaven::class.java)
            .forEach {
                it.mustRunAfter(signTasks)
            }
    }
}



