plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.20")
}

gradlePlugin {
    plugins {
        register("daisycoreKotlinLibrary") {
            id = "daisycore.kotlin-library"
            implementationClass = "DaisyCoreKotlinLibraryPlugin"
        }
        register("daisycorePaperPlugin") {
            id = "daisycore.paper-plugin"
            implementationClass = "DaisyCorePaperPluginConvention"
        }
    }
}
