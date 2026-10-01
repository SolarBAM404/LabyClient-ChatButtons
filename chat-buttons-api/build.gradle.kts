plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "io.gitlab.solarm404"
providers.gradleProperty("apiVersion").orNull?.let { version = it }

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "chat-buttons-api", version.toString())

    pom {
        name.set("LabyMod Chat Buttons Paper API")
        description.set("Paper API for server-provided chat buttons in the LabyMod addon")
        url.set("https://github.com/SolarBAM404/LabyClient-ChatButtons")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("SolarBAM404")
                name.set("SolarBAM404")
                url.set("https://github.com/SolarBAM404")
            }
        }

        scm {
            url.set("https://github.com/SolarBAM404/LabyClient-ChatButtons")
            connection.set("scm:git:https://github.com/SolarBAM404/LabyClient-ChatButtons.git")
            developerConnection.set(
                "scm:git:ssh://git@github.com/SolarBAM404/LabyClient-ChatButtons.git"
            )
        }
    }
}
