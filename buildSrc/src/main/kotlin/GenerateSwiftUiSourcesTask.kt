import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GenerateSwiftUiSourcesTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDir: DirectoryProperty

    @get:Input
    abstract val frameworkModuleName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val moduleName = frameworkModuleName.get()
        val sourceRoot = sourceDir.get().asFile
        val targetRoot = outputDir.get().asFile

        targetRoot.mkdirs()

        sourceRoot.listFiles { file -> file.isFile && file.extension == "swift" }
            ?.sortedBy { it.name }
            ?.forEach { sourceFile ->
                val rendered = sourceFile.readText()
                    .replace("@preconcurrency import PaginautCore", "@preconcurrency import $moduleName")
                    .replace("import PaginautCore", "import $moduleName")

                targetRoot.resolve(sourceFile.name).writeText(rendered)
            }

        logger.lifecycle(
            "Generated Paginaut SwiftUI sources for module '{}' into {}",
            moduleName,
            targetRoot,
        )
    }
}
