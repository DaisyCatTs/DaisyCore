plugins {
    id("daisycore.paper-plugin")
}

tasks.test {
    // Mockito's inline mock maker needs self-attach on this JDK/Windows setup.
    jvmArgs("-Djdk.attach.allowAttachSelf=true")
}

dependencies {
    api(project(":platform-base"))
    api(project(":platform-runtime"))
    api(project(":platform-text"))
    implementation(project(":platform-placeholders"))
    testImplementation("org.mockito:mockito-core:5.23.0")
}
