import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun
import kotlin.concurrent.thread

plugins {
    id("org.springframework.boot") version "2.3.3.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.spring") version "1.4.0"
}

group = "ms.safi.spring"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation(project(":spring-boot-starter-spa"))

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

abstract class RunSpaDevServerTask : DefaultTask() {
    var packageManagerCommand: String = "npm"
    var scriptName: String = "start"
    @InputDirectory
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
}

val spaDevServerTask = tasks.register<RunSpaDevServerTask>("spaDevServer")

tasks.withType<BootRun> {
    dependsOn(spaDevServerTask)
}