plugins {
    id("daisycore.paper-plugin")
}

dependencies {
    api(project(":platform-base"))
    api(project(":platform-runtime"))
    api(project(":platform-text"))
    api(project(":platform-placeholders"))
    api(project(":platform-items"))
    api(project(":platform-command"))
    api(project(":platform-menu"))
    api(project(":platform-scoreboard"))
    api(project(":platform-tablist"))
}

publishing {
    publications.named<MavenPublication>("maven") {
        artifactId = "DaisyCore"
    }
}
