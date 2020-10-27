package ms.safi.spring.spa.devserver

import java.io.File
import java.util.concurrent.Executor
import javax.annotation.PostConstruct

class StartDevServerTask(private val executor: Executor) : Runnable {

    @PostConstruct
    fun startServer() {
        executor.execute(this)
    }

    override fun run() {
        val directory = File("src/js")
        println("running npm run start in directory: ${directory.absolutePath}")
        val processBuilder = ProcessBuilder()
                .command("npm", "run", "start")
                .directory(directory)
                .inheritIO()
        with(processBuilder.environment()) {
            put("BROWSER", "none")
            put("CI", "true") // Required to prevent WebpackDevServer from exiting as soon as stdin ends. Better way to do this?
        }

        val process = processBuilder.start()
        Runtime.getRuntime().addShutdownHook(Thread(process::destroy))
        process.waitFor()
    }
}



