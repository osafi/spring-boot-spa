package ms.safi.spa.devserver

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.concurrent.thread

open class RunSpaDevServerTask : DefaultTask() {
    var packageManagerCommand: String = "npm"
    var scriptName: String = "start"
    var spaRoot: File = project.file("${project.projectDir}/src/js")
    var readyText: String = "Compiled successfully!"

    @TaskAction
    fun run() {
        val workingDirectory = spaRoot
        val port = 3000
        val command = listOf(packageManagerCommand, "run", scriptName)
        logger.warn("running '${command.joinToString(" ")}' in directory '${workingDirectory.absolutePath}'")
        val processBuilder = ProcessBuilder()
                .command(command)
                .redirectErrorStream(true)
                .directory(workingDirectory)
        with(processBuilder.environment()) {
            put("BROWSER", "none")
            put("PORT", port.toString())
        }

        val process = processBuilder.start()

        val inputReader = process.inputStream.bufferedReader()
        var isReady = false
        while (!isReady) {
            val line = inputReader.readLine() ?: break
            if (line.contains(readyText)) {
                logger.warn("process in ready state")
                isReady = true
            }
        }

        if (!isReady) {
            process.waitFor()
            val exitCode = process.exitValue()
            throw GradleException("The command '${command.joinToString(" ")}' exited before reaching ready state - exit code: $exitCode")
        }

        val thisTaskIsLastInTaskGraph = project.gradle.taskGraph.allTasks.last() == this
        if (thisTaskIsLastInTaskGraph) {
            inputReader.forEachLine(logger::warn)
        } else {
            logger.warn("moving output capture to daemon thread")
            thread(start = true, isDaemon = true) { inputReader.forEachLine(logger::warn) }
        }
    }

    companion object {
        const val NAME = "spaDevServer"
    }
}