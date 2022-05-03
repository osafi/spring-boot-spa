import com.github.psxpaul.task.ExecFork
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    id("com.github.hesch.execfork") version "0.1.15"
}

group = "ms.safi.spring"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

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

val spaDevServerTask = tasks.register<ExecFork>("spaDevServer") {
    val port = 3000 // findOpenPort()
    executable = "npm"
    args = mutableListOf("run", "start")
    workingDir = file("$projectDir/src/js")
    timeout = 300
    waitForOutput = "Compiled successfully!"
    waitForPort = port
    environment = mapOf(
            "BROWSER" to "none",
            "PORT" to port
    )
}

tasks.withType<BootRun> {
    dependsOn(spaDevServerTask)
}
