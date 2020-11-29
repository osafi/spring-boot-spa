plugins {
    id("org.springframework.boot") version "2.3.3.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
    kotlin("jvm") version "1.4.20" apply false
    kotlin("plugin.spring") version "1.4.20" apply false
    kotlin("kapt") version "1.4.20" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}