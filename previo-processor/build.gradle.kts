import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlinJvm)
    `maven-publish`
}

group = "com.github.ugurbuga"
version = "1.0.0-alpha01"

dependencies {
    implementation(libs.kotlin.test)
    // KSP API dependency
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.20-1.0.31")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
            groupId = "com.github.ugurbuga"
            artifactId = "previo-processor"
            version = "1.0.0-alpha01"
        }
    }
}
