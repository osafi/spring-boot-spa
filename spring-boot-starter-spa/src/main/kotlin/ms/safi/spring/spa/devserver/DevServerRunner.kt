package ms.safi.spring.spa.devserver

import org.slf4j.LoggerFactory
import java.io.File
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.Executor
import javax.annotation.PostConstruct

class DevServerRunner(private val executor: Executor) : Runnable {
    companion object {
        private val logger = LoggerFactory.getLogger(DevServerRunner::class.java)
    }

    @PostConstruct
    fun startServer() {
        executor.execute(this)
    }

    override fun run() {
        val directory = File("src/js")
        logger.info("running npm run start in directory '${directory.absolutePath}'")
        val processBuilder = ProcessBuilder()
                .command("npm", "run", "start")
                .directory(directory)
                .redirectOutput(Redirect.INHERIT)
        with(processBuilder.environment()) {
            put("BROWSER", "none")
        }

        val process = processBuilder.start()
        Runtime.getRuntime().addShutdownHook(Thread(process::destroy))
        process.waitFor()
    }
}



