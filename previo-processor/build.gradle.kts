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
