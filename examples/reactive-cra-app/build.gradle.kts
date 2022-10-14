import com.github.psxpaul.task.ExecFork
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot") version "2.7.4"
    id("io.spring.dependency-management") version "1.0.14.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("com.github.psxpaul.execfork") version "0.2.0"
}

repositories {
    mavenCentral()
}

group = "ms.safi.spring"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("ms.safi.spring:spring-boot-starter-spa:0.0.8")
    implementation("ms.safi.spring:spring-boot-spa-devserver:0.0.8")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
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

val installFrontend = tasks.register<Exec>("installFrontend") {
    inputs.file(file("$projectDir/src/js/yarn.lock"))
    inputs.file(file("$projectDir/src/js/package.json"))
    outputs.dir(file("$projectDir/src/js/node_modules"))
    workingDir = file("$projectDir/src/js")
    commandLine("yarn", "install")
}

val buildFrontend = tasks.register<Exec>("buildFrontend") {
    dependsOn(installFrontend)
    inputs.dir(file("$projectDir/src/js/src"))
    inputs.dir(file("$projectDir/src/js/public"))
    outputs.dir("$projectDir/src/js/build")
    workingDir = file("$projectDir/src/js")
    commandLine("yarn", "build")
}

val copyFrontend = tasks.register<Sync>("copyFrontend") {
    dependsOn(buildFrontend)
    from(file("$projectDir/src/js/build"))
    into(file("$buildDir/resources/main/static"))
    doLast {
        println("copied built frontend to static resources")
    }
}

val spaDevServerTask = tasks.register<ExecFork>("spaDevServer") {
    dependsOn(installFrontend)

    val port = 3000 // findOpenPort()
    executable = "yarn"
    args = mutableListOf("start")
    workingDir = file("$projectDir/src/js")
    timeout = 300
    waitForOutput = "Compiled successfully!"
    waitForPort = port
    environment = mapOf(
            "BROWSER" to "none",
            "PORT" to port
    )
}

val bootRun = tasks.withType<BootRun> {
    if (project.hasProperty("dev")) {
        dependsOn(spaDevServerTask)
        systemProperty("spring.profiles.active", "dev")
    } else {
        dependsOn(copyFrontend)
    }
}
