import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.ugurbuga"
version = "1.0.0-alpha01"

dependencies {
    implementation(libs.kotlin.test)
    // KSP API dependency
    implementation(libs.kspApi)
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.ugurbuga", "previo-processor", "1.0.0-alpha01")

    pom {
        name.set("Previo Processor")
        description.set("Automated KSP Symbol Processor for Previo Preview Explorer")
        url.set("https://github.com/ugurbuga/previo")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ugurbuga")
                name.set("Ugur Buga")
                email.set("ugurbuga@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/ugurbuga/previo.git")
            developerConnection.set("scm:git:ssh://github.com/ugurbuga/previo.git")
            url.set("https://github.com/ugurbuga/previo")
        }
    }
}
