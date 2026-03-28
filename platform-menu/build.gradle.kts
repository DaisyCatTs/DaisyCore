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
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.133.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
