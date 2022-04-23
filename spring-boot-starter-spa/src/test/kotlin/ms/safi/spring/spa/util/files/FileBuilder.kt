package ms.safi.spring.spa.util.files

import java.nio.file.Files
import java.nio.file.Path

class FileBuilder(rootOnClasspath: Boolean) {
    private val createdFiles: MutableList<Path> = mutableListOf()
    private val rootPath: Path? = if (rootOnClasspath) classpathPath() else null

    operator fun invoke(path: String, fileContent: String = "") {
        this.invoke(Path.of(path), fileContent)
    }

    operator fun invoke(path: Path, fileContent: String = "") {
        val pathToWrite = rootPath?.resolve(path) ?: path
        val greatestAncestorToDelete = getGreatestAncestorThatNeedsToBeCreated(pathToWrite)

        Files.createDirectories(pathToWrite.parent)
        Files.write(pathToWrite, fileContent.toByteArray())

        createdFiles.add(pathToWrite)
        greatestAncestorToDelete?.also(createdFiles::add)
    }

    fun deleteCreatedFiles() {
        createdFiles.forEach {
            when {
                Files.isDirectory(it) -> it.toFile().deleteRecursively()
                else -> Files.deleteIfExists(it)
            }
        }
    }

    private fun classpathPath(): Path = Path.of(FileBuilder::class.java.classLoader.getResource(".").path)

    private fun getGreatestAncestorThatNeedsToBeCreated(path: Path): Path? {
        var parent = path.parent ?: return null
        val stack = ArrayDeque<Path>()

        while (Files.notExists(parent)) {
            stack.addLast(parent)
            parent = parent.parent
        }

        return stack.lastOrNull()
    }

}