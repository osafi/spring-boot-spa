plugins {
    id("org.springframework.boot") version "2.6.6" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    kotlin("jvm") version "1.6.20" apply false
    kotlin("plugin.spring") version "1.6.20" apply false
    kotlin("kapt") version "1.6.20" apply false
    id("com.github.ben-manes.versions") version "0.42.0" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}