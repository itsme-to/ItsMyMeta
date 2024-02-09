plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.0.0"
    id("com.willfp.libreforge-gradle-plugin") version "1.0.2"
}

group = "ru.oftendev"
version = findProperty("version")!!
val libreforgeVersion = findProperty("libreforge-version")

base {
    archivesName.set(project.name)
}

dependencies {
    rootProject.subprojects {
        implementation(this)
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
        maven { url = uri("https://repo.bg-software.com/repository/api/") }
        maven { url = uri("https://ci.ender.zone/plugin/repository/everything/") }
    }

    dependencies {
        compileOnly("com.willfp:eco:6.53.0")
        compileOnly("org.jetbrains:annotations:24.0.1")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
        compileOnly ("world.bentobox:bentobox:1.22.0-SNAPSHOT")
        compileOnly ("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")
        compileOnly (fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
        compileOnly ("com.bgsoftware:SuperiorSkyblockAPI:2022.9")
        compileOnly ("com.massivecraft:Factions:1.6.9.5-U0.6.21")
        compileOnly ("net.william278:HuskTowns:2.1")
        compileOnly("com.github.MilkBowl:VaultAPI:1.7")
        implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")
    }

    java {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks {
        shadowJar {
            relocate("com.willfp.libreforge.loader", "ru.oftendev.itsmymeta.libreforge.loader")
        }

        compileKotlin {
            kotlinOptions {
                jvmTarget = "17"
            }
        }

        compileJava {
            options.isDeprecation = true
            options.encoding = "UTF-8"

            dependsOn(clean)
        }

        processResources {
            filesMatching(listOf("**plugin.yml", "**eco.yml")) {
                expand(
                    "version" to project.version,
                    "libreforgeVersion" to libreforgeVersion,
                    "pluginName" to rootProject.name
                )
            }
        }

        build {
            dependsOn(shadowJar)
        }
    }
}