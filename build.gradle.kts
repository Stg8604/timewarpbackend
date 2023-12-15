import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    id("com.diffplug.spotless") version "6.12.0"
}

group = "delta.timewrap"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("com.sendgrid:sendgrid-java:4.0.1")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.security:spring-security-oauth2-client")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.0")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        ktfmt()
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

task("preCommitHook") {
    println("PreCommitHook")
    exec {
        commandLine("cp", "./.githooks/pre-commit", "./.git/hooks")
    }
    println("Added")
}

task("commitMsgHook") {
    println("commitMsgHook")
    exec {
        commandLine("cp", "./.githooks/commit-msg", "./.git/hooks")
    }
    println("Added")
}
