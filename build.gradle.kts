plugins {
    java
    id("com.diffplug.spotless") version "7.2.1" apply false
}

allprojects {
    group = "me.solar.laby.chatbuttons"
    version = "1.0.0-SNAPSHOT"
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://dist.labymod.net/api/v1/maven/release/")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")
    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.28.0")
        }
    }
    extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named("build") {
    dependsOn(gradle.includedBuild("client-addon").task(":build"))
}

tasks.register<Copy>("createReleaseJar") {
    dependsOn(gradle.includedBuild("client-addon").task(":createReleaseJar"))
    from(layout.projectDirectory.dir("client-addon/build/libs")) {
        include("*-release.jar")
    }
    into(layout.buildDirectory.dir("libs"))
}
