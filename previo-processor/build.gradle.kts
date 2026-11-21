import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlinJvm)
    `maven-publish`
}

group = "com.github.ugurbuga"
version = "1.0.0-alpha05"

dependencies {
    implementation(libs.kotlin.test)
    // KSP API dependency
    implementation(libs.kspApi)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
            groupId = "com.github.ugurbuga"
            artifactId = "previo-processor"
            version = "1.0.0-alpha05"
        }
    }
}
