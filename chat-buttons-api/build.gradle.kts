plugins {
    `java-library`
    `maven-publish`
}

providers.gradleProperty("apiVersion").orNull?.let { version = it }

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "chat-buttons-api"
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri(
                "https://maven.pkg.github.com/" +
                    (System.getenv("GITHUB_REPOSITORY") ?: "SolarBAM404/LabyClient-ChatButtons")
                        .lowercase()
            )
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
