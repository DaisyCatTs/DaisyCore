plugins {
    id("daisycore.paper-plugin")
}

dependencies {
    api(project(":platform-base"))
    api(project(":platform-runtime"))
    api(project(":platform-text"))
    api(project(":platform-placeholders"))
}
