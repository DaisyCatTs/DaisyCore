import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class DaisyCorePaperPluginConvention : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("daisycore.kotlin-library")
        target.dependencies {
            add("compileOnly", "io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
            add("testImplementation", "io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
        }
    }
}
