buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.springframework.boot") version "2.6.7" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    kotlin("jvm") version "1.6.21" apply false
    kotlin("plugin.spring") version "1.6.21" apply false
    kotlin("kapt") version "1.6.21" apply false
    id("com.vanniktech.maven.publish") version "0.19.0" apply false
    id("com.github.ben-manes.versions") version "0.42.0" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }

    // optional configuration based on:
    // https://github.com/spring-projects/spring-boot/blob/main/buildSrc/src/main/java/org/springframework/boot/build/optional/OptionalDependenciesPlugin.java
    val optional: Configuration by configurations.creating {
        isCanBeConsumed = false
        isCanBeResolved = false
    }

    val thisProject = this
    plugins.withType(JavaPlugin::class.java) {
        val sourceSets = thisProject.extensions.getByType(JavaPluginExtension::class.java).sourceSets
        sourceSets.all {
            thisProject.configurations.getByName(compileClasspathConfigurationName).extendsFrom(optional)
            thisProject.configurations.getByName(runtimeClasspathConfigurationName).extendsFrom(optional)
        }
    }
}




