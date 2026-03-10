plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.2"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "github.vanes430"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")
    compileOnly("com.github.retrooper:packetevents-velocity:2.11.2")
    compileOnly("io.netty:netty-all:4.1.100.Final")
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-brigadier:1.0.0-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:deprecation"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    // Exclude redundant metadata
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/MANIFEST.MF")
    exclude("META-INF/versions/9/module-info.class")
    exclude("module-info.class")
}

spotless {
    java {
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching(listOf("plugin.yml", "velocity-plugin.json")) {
        expand(props)
    }
}
