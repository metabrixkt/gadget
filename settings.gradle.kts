pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "gadget"


// Use the unpublished 26.3 port from the sibling checkout for both the API and processor: https://github.com/metabrixkt/owo-lib
//includeBuild("../owo-lib") {
//    dependencySubstitution {
//        substitute(module("io.wispforest:owo-lib")).using(project(":"))
//    }
//}
