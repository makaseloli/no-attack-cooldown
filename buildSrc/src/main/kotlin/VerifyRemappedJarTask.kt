import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.util.zip.ZipFile

abstract class VerifyRemappedJarTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val remappedJar: RegularFileProperty

    @TaskAction
    fun verify() {
        val jarFile = remappedJar.get().asFile
        val forbiddenNames = listOf(
            "net/minecraft/resources/ResourceLocation",
            "net/minecraft/world/entity/player/Player"
        )

        ZipFile(jarFile).use { zip ->
            val badEntry = zip.entries().asSequence()
                .filter { it.name.endsWith(".class") }
                .firstOrNull { entry ->
                    zip.getInputStream(entry).use { input ->
                        val classBytes = input.readBytes().toString(Charsets.ISO_8859_1)
                        forbiddenNames.any(classBytes::contains)
                    }
                }

            if (badEntry != null) {
                throw GradleException(
                    "${jarFile.name} still contains Mojang runtime names in ${badEntry.name}. " +
                        "Use the remapJar output from build/libs, not the development jar from build/devlibs."
                )
            }
        }
    }
}
