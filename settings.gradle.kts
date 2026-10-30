pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Multi-App"
include(":app", ":qube-android-lib")
project(":qube-android-lib").projectDir = file("engine/QubeVM/qube-android-lib")
