package ms.safi.spring.spa.devserver

import org.slf4j.LoggerFactory
import java.io.File
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.Executor
import javax.annotation.PostConstruct

// Should this be done via a custom gradle Exec task instead of in app process? https://stackoverflow.com/a/25308762
class DevServerRunner(
        private val executor: Executor,
        private val properties: DevServerConfigurationProperties
) : Runnable {
    companion object {
        private val logger = LoggerFactory.getLogger(DevServerRunner::class.java)
    }

    @PostConstruct
    fun startServer() {
        executor.execute(this)
    }

    override fun run() {
        val workingDirectory = File(properties.runner.workingDirectory)
        val pkgManagerCommand = properties.runner.packageManagerCommand.name.toLowerCase()
        val script = properties.runner.scriptName
        val port = properties.port

        val command = listOf(pkgManagerCommand, "run", script)
        logger.info("running '${command.joinToString(" ")}' in directory '${workingDirectory.absolutePath}'")
        val processBuilder = ProcessBuilder()
                .command(command)
                .directory(workingDirectory)
                .redirectOutput(Redirect.INHERIT)
        with(processBuilder.environment()) {
            put("BROWSER", "none")
            put("PORT", port.toString())
        }

        val process = processBuilder.start()
        Runtime.getRuntime().addShutdownHook(Thread(process::destroy))
        process.waitFor()
    }
}



