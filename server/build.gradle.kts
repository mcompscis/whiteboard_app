import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.4"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"

    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

application {
    // Define the main class for the server application.
    mainClass.set("cs.project.ServerLauncher")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Taken from: https://stackoverflow.com/questions/37173218/how-to-configure-gradle-to-output-total-number-of-tests-executed
tasks.withType<AbstractTestTask> {
    afterSuite(
        KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            // Only execute on the outermost suite
            if (desc.parent == null) {
                println(" **** Result: ${result.resultType} ****")
                println("  >    Tests: ${result.testCount}")
                println("  >   Passed: ${result.successfulTestCount}")
                println("  >   Failed: ${result.failedTestCount}")
                println("  >  Skipped: ${result.skippedTestCount}")
            }
        })
    )
}

