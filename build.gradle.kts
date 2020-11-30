plugins {
    id("org.springframework.boot") version "2.4.0" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
    kotlin("jvm") version "1.4.10" apply false
    kotlin("plugin.spring") version "1.4.10" apply false
    kotlin("kapt") version "1.4.10" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}