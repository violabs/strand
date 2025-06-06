// build.gradle.kts (root)
plugins {
    kotlin("plugin.spring") version "2.0.20"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

allprojects {
    group = "com.example"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    dependencies {
//        implementation("org.springframework.boot:spring-boot-starter-web")
//        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}