plugins {
    id("com.diffplug.spotless") version "7.2.1"
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

spotless {
    java {
        googleJavaFormat("1.28.0")
    }
}

group = "me.solar.laby.chatbuttons"
version = "1.0.0"
val minecraftVersions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

labyMod {
    defaultPackageName = "me.solar.laby.chatbuttons"

    minecraft {
        registerVersion(minecraftVersions.toTypedArray()) {
            runs {
                getByName("client") {
                    // Set devLogin = true locally to test against an authenticated server.
                }
            }
        }
    }

    addonInfo {
        namespace = "chatbuttons"
        displayName = "Chat Buttons"
        author = "Chat Buttons"
        description = "Shows server-provided action buttons in the chat screen."
        minecraftVersion = "26.2"
        version = rootProject.version.toString()
    }

}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")
    plugins.apply("com.diffplug.spotless")
    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.28.0")
        }
    }
    group = rootProject.group
    version = rootProject.version
    extensions.findByType(org.gradle.api.plugins.JavaPluginExtension::class.java)?.apply {
        sourceCompatibility = org.gradle.api.JavaVersion.VERSION_21
        targetCompatibility = org.gradle.api.JavaVersion.VERSION_21
    }
}
