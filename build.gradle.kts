import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val javaVersion = 17

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")

    val graalVersion = "23.0.1"
    implementation("org.graalvm.sdk:graal-sdk:$graalVersion")
    implementation("org.graalvm.js:js:$graalVersion")
    implementation("org.graalvm.truffle:truffle-api:$graalVersion")

    testImplementation("org.assertj:assertj-core:3.24.2")
    val junitVersion = "5.10.0"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

tasks {
    // Run reobfJar on build
    build {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(javaVersion)
        options.compilerArgs.add("-parameters")
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
    shadowJar {
        mergeServiceFiles()
    }
    test {
        useJUnitPlatform()
    }
    runServer {
        jvmArgs("-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=false")
    }
    runMojangMappedServer {
        jvmArgs("-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=false")
    }
}

paper {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "dev.benndorf.minitestframework.MiniTestFramework"
    bootstrapper = "dev.benndorf.minitestframework.MiniTestFrameworkBootstrap"
    apiVersion = "1.20"
    authors = listOf("MiniDigger")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}
