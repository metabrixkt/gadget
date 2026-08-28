plugins {
    alias(libs.plugins.fabric.loom)
    `maven-publish`
}

version = providers.gradleProperty("mod-version")
    .flatMap { modVersion -> libs.versions.minecraft.map { minecraftVersion -> "$modVersion+$minecraftVersion" } }
    .get()
group = providers.gradleProperty("group-id").get()

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven("https://api.modrinth.com/maven") {
        content {
            includeGroup("maven.modrinth")
        }
    }
    mavenLocal()
    maven("https://maven.wispforest.io/releases/")
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.blamejared.com")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.ladysnake.org/releases")
    maven("https://maven.architectury.dev/")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://jitpack.io")
    maven("https://maven.jamieswhiteshirt.com/libs-release") {
        content {
            includeGroup("com.jamieswhiteshirt")
        }
    }
}

private val testModSourceSetName = "testmod"

sourceSets {
    create(testModSourceSetName) {
        runtimeClasspath = files(runtimeClasspath, main.map { it.runtimeClasspath })
        compileClasspath = files(compileClasspath, main.map { it.compileClasspath })
    }
}

dependencies {
    minecraft(libs.minecraft)

    annotationProcessor(libs.owo.lib)

    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(libs.owo.lib)

    include(libs.fabric.permissions.api)
    include(libs.mapping.io)
    include(libs.auoeke.result)
    include(libs.auoeke.unsafe)
    include(libs.auoeke.reflect)

    compileOnly(libs.quiltflower)

    runtimeOnly(libs.modmenu)

//    runtimeOnly(libs.patchouli)
//    runtimeOnly(libs.trinkets)
//    runtimeOnly(libs.rei)
//    runtimeOnly(libs.architectury.fabric)
//    runtimeOnly(libs.things)
    runtimeOnly(libs.cloth.config)
//    runtimeOnly(libs.auth.me)

    "testmodImplementation"(sourceSets.main.get().output)
}

configurations {
    implementation {
        extendsFrom(include)
    }
}

loom {
    runs {
        register("testmodClient") {
            client()
            displayName = "Testmod Client"
            sourceSet = testModSourceSetName
        }
        register("testmodServer") {
            server()
            displayName = "Testmod Server"
            sourceSet = testModSourceSetName
        }
    }

    accessWidenerPath = file("src/main/resources/gadget.accesswidener")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(inputs.properties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

private val archiveBaseName = providers.gradleProperty("archive-base-name")

base {
    archivesName = archiveBaseName
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${archiveBaseName.get()}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        providers.environmentVariable("MAVEN_URL").orNull?.let { mavenUrl ->
            maven(mavenUrl) {
                credentials {
                    username = providers.environmentVariable("MAVEN_USERNAME").orNull
                    password = providers.environmentVariable("MAVEN_PASSWORD").orNull
                }
            }
        }
    }
}
