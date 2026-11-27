plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.mavenPublish)
}

dependencies {
    implementation(libs.kotlin.test)
    // KSP API dependency
    implementation(libs.kspApi)
}

mavenPublishing {
    // publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    // if (project.hasProperty("signing.keyId") || System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey") != null) {
    //     signAllPublications()
    // }

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
                name.set("Uğur Buğa")
                email.set("ugurbuga00@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/ugurbuga/previo.git")
            developerConnection.set("scm:git:ssh://github.com/ugurbuga/previo.git")
            url.set("https://github.com/ugurbuga/previo")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ugurbuga/previo")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
