plugins {
    `java-gradle-plugin`
    kotlin("jvm")
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "org.samples.greeting"
            implementationClass = "org.gradle.GreetingPlugin"
        }
    }
}