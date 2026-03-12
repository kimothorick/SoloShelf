pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Official Readium repository for 3.x
        maven { url = uri("https://dl.cloudsmith.io/public/readium/readium-kotlin-toolkit/maven/") }
        // Required for transitive dependencies like AndroidPdfViewer
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SoloShelf"
include(":app")
