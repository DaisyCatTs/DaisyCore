plugins {
    id("daisycore.paper-plugin")
}

dependencies {
    api(project(":platform-base"))
    api(project(":platform-runtime"))
    api(project(":platform-text"))
    api(project(":platform-items"))
    implementation(project(":platform-placeholders"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.84.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation("org.mockito:mockito-inline:5.2.0")
}
