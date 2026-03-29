plugins {
    id("daisycore.paper-plugin")
}

dependencies {
    api(project(":platform-base"))
    api(project(":platform-runtime"))
    api(project(":platform-text"))
    api(project(":platform-placeholders"))
    implementation(project(":platform-items"))
    implementation(project(":platform-scoreboard"))
    testImplementation("org.mockito:mockito-inline:5.2.0")
}
