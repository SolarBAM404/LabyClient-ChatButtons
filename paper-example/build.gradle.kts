plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation(project(":chat-buttons-api"))
    implementation("com.google.code.gson:gson:2.11.0")
}

tasks.shadowJar { archiveClassifier.set("") }
tasks.build { dependsOn(tasks.shadowJar) }
