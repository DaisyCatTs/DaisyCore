plugins {
    base
}

allprojects {
    group = "cat.daisy"
    version = "0.1.0-SNAPSHOT"
}

tasks.register("quality") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs the full DaisyCore verification suite."
    dependsOn(gradle.includedBuild("build-logic").task(":check"))
    dependsOn(subprojects.map { "${it.path}:check" })
}
