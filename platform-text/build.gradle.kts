plugins {
    id("daisycore.kotlin-library")
}

dependencies {
    api(project(":platform-base"))
    api("net.kyori:adventure-api:4.26.1")
    api("net.kyori:adventure-text-minimessage:4.26.1")
}
