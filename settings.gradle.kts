pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
    }
}

rootProject.name = "DaisyCore"

includeBuild("build-logic")

include(
    "platform-base",
    "platform-runtime",
    "platform-text",
    "platform-placeholders",
    "platform-items",
    "platform-command",
    "platform-menu",
    "platform-scoreboard",
    "platform-tablist",
    "platform-packet",
    "platform-all",
    "example-plugin",
)
