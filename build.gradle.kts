import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.4.1"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")

    val graalVersion = "21.2.0"
    implementation("org.graalvm.sdk:graal-sdk:$graalVersion")
    implementation("org.graalvm.js:js:$graalVersion")
    implementation("org.graalvm.truffle:truffle-api:$graalVersion")

    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
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
        options.release.set(17)
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
}


bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "dev.benndorf.minitestframework.MiniTestFramework"
    apiVersion = "1.19"
    authors = listOf("MiniDigger")
    commands {
        register("minitest") {
            description = "Root command for MiniTestFramework"
            aliases = listOf("mtest")
            permission = "minitestframework"
            usage = "minitest <reload>"
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
