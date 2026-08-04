pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // مخزن میرور پشتیبان در صورت بلاک بودن Maven Central
        maven { url = uri("https://repo1.maven.org/maven2/") }
    }
}

rootProject.name = "EyeGuard"
include(":app")
