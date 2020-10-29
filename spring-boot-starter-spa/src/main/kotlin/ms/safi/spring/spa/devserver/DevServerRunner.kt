package ms.safi.spring.spa.devserver

import ms.safi.spring.spa.devserver.DevServerConfigurationProperties.RunnerProperties
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.Executor
import javax.annotation.PostConstruct

class DevServerRunner(private val executor: Executor, private val properties: RunnerProperties) : Runnable {
    companion object {
        private val logger = LoggerFactory.getLogger(DevServerRunner::class.java)
    }

    @PostConstruct
    fun startServer() {
        executor.execute(this)
    }

    override fun run() {
        val workingDirectory = File(properties.workingDirectory)
        val command = listOf(properties.packageManagerCommand.name.toLowerCase(), "run", properties.scriptName)
        logger.info("running '${command.joinToString(" ")}' in directory '${workingDirectory.absolutePath}'")
        val processBuilder = ProcessBuilder()
                .command(command)
                .directory(workingDirectory)
                .redirectOutput(Redirect.INHERIT)
        with(processBuilder.environment()) {
            put("BROWSER", "none")
        }

        val process = processBuilder.start()
        Runtime.getRuntime().addShutdownHook(Thread(process::destroy))
        process.waitFor()
    }
}



