import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class DaisyCoreKotlinLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply(JavaLibraryPlugin::class.java)
            pluginManager.apply("maven-publish")

            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(21)
            }

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
                withSourcesJar()
                withJavadocJar()
            }

            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                    freeCompilerArgs.addAll(
                        "-opt-in=kotlin.RequiresOptIn",
                        "-jvm-default=no-compatibility",
                    )
                }
            }

            dependencies {
                add("implementation", "org.jetbrains.kotlin:kotlin-stdlib:2.3.20")
                add("testImplementation", "org.jetbrains.kotlin:kotlin-test:2.3.20")
                add("testImplementation", "org.junit.jupiter:junit-jupiter:5.13.4")
                add("testImplementation", "org.mockito.kotlin:mockito-kotlin:5.4.0")
            }

            tasks.named("test", org.gradle.api.tasks.testing.Test::class.java).configure {
                useJUnitPlatform()
            }

            extensions.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        from(components.getByName("java"))
                        groupId = project.group.toString()
                        artifactId = project.name
                        version = project.version.toString()
                    }
                }
            }
        }
    }
}
