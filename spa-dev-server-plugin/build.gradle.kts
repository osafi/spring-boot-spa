plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
    kotlin("jvm")
}

version = "0.0.1-SNAPSHOT"

gradlePlugin {
    plugins {
        create("spaDevServerPlugin") {
            id = "ms.safi.spa.devserver"
            implementationClass = "ms.safi.spa.devserver.SpaDevServerPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/osafi/spring-boot-spa"
    vcsUrl = "https://github.com/osafi/spring-boot-spa"
    description = "Single Page App dev server runner"

    (plugins) {
        "spaDevServerPlugin" {
            displayName = "SPA Dev Server Runner plugin"
            tags = listOf("spa", "single-page-app", "react", "spring-boot")
        }
    }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))
}