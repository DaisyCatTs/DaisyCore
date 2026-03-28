plugins {
    id("daisycore.paper-plugin")
}

dependencies {
    api(project(":platform-base"))
    api(project(":platform-runtime"))
    api(project(":platform-text"))
    implementation(project(":platform-placeholders"))
    testImplementation("org.mockito:mockito-core:5.20.0")
}
