import com.github.psxpaul.task.ExecFork
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	kotlin("jvm")
	kotlin("plugin.spring")
	id("com.github.psxpaul.execfork") version "0.2.0"
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
	implementation(project(":spring-boot-starter-spa"))

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
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
