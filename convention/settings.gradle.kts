dependencyResolutionManagement {
    repositories {
        google()
        gradlePluginPortal() // so that external plugins can be resolved in dependencies section
        mavenCentral()
    }
    // Sharing the root project version catalog
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "convention-plugins"
