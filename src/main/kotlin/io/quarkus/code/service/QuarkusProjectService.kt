package io.quarkus.code.service

import io.quarkus.code.misc.create.CreateProject
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.devtools.commands.data.QuarkusCommandException
import io.quarkus.devtools.project.BuildTool
import io.quarkus.devtools.project.compress.QuarkusProjectCompress
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuarkusProjectService {
    companion object {
        private const val MVNW_RESOURCES_DIR = "/creator/mvnw"
        private const val MVNW_WRAPPER_DIR = ".mvn/wrapper"
        private const val MVNW_WRAPPER_JAR = "$MVNW_WRAPPER_DIR/maven-wrapper.jar"
        private const val MVNW_WRAPPER_PROPS = "$MVNW_WRAPPER_DIR/maven-wrapper.properties"
        private const val MVNW_WRAPPER_DOWNLOADER = "$MVNW_WRAPPER_DIR/MavenWrapperDownloader.java"
        private const val MVNW_CMD = "mvnw.cmd"
        private const val MVNW = "mvnw"

        // Gradlew constants
        private const val GRADLEW_RESOURCES_DIR = "/creator/gradlew"
        private const val GRADLEW_WRAPPER_DIR = "gradle/wrapper"
        private const val GRADLEW_WRAPPER_JAR = "$GRADLEW_WRAPPER_DIR/gradle-wrapper.jar"
        private const val GRADLEW_WRAPPER_PROPS = "$GRADLEW_WRAPPER_DIR/gradle-wrapper.properties"
        private const val GRADLEW_BAT = "gradlew.bat"
        private const val GRADLEW = "gradlew"
    }

    @Inject
    internal lateinit var extensionCatalog: QuarkusExtensionCatalogService

    fun create(projectDefinition: ProjectDefinition): ByteArray {
        QuarkusExtensionCatalogService.checkPlatformInitialization()
        val path = createTmp(projectDefinition)
        val time = System.currentTimeMillis() - 24 * 3600000
        val zipPath = Files.createTempDirectory("zipped-").resolve("project.zip")
        QuarkusProjectCompress.zip(path, zipPath, true, time)
        return Files.readAllBytes(zipPath)
    }

    fun createTmp(projectDefinition: ProjectDefinition, isGitHub: Boolean = false): Path {
        val location = Files.createTempDirectory("generated-").resolve(projectDefinition.artifactId)
        createProject(projectDefinition, location, isGitHub)
        return location;
    }

    private fun createProject(projectDefinition: ProjectDefinition, projectFolderPath: Path, gitHub: Boolean) {
        val extensions = checkAndMergeExtensions(projectDefinition)
        val sourceType = CreateProject.determineSourceType(extensions)
        val buildTool = BuildTool.valueOf(projectDefinition.buildTool)
        val codestarts = HashSet<String>()
        if(gitHub) {
            codestarts.add("github-action")
        }
        try {
            val result = CreateProject(projectFolderPath, QuarkusExtensionCatalogService.descriptor)
                    .groupId(projectDefinition.groupId)
                    .artifactId(projectDefinition.artifactId)
                    .version(projectDefinition.version)
                    .sourceType(sourceType)
                    .codestartsEnabled(true)
                    .buildTool(buildTool)
                    .codestarts(codestarts)
                    .javaTarget("11")
                    .className(projectDefinition.className)
                    .extensions(extensions)
                    .noExamples(projectDefinition.noExamples)
                    .setValue("path", projectDefinition.path)
                    .execute()
            if (!result.isSuccess) {
                throw IOException("Error during Quarkus project creation")
            }
        } catch (e: QuarkusCommandException) {
            throw IOException("Error during Quarkus project creation", e)
        }

    }

    private fun checkAndMergeExtensions(projectDefinition: ProjectDefinition): Set<String> {
        return extensionCatalog.checkAndMergeExtensions(projectDefinition.extensions, projectDefinition.shortExtensions)
    }

}
