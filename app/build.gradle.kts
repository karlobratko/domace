import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    application
}

group = "hr.algebra"
version = "0.0.1"

repositories {
    mavenCentral()
}

application {
    mainClass.set("hr.algebra.domace.infrastructure.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
}

kotlin {
    jvmToolchain(19)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true

    // TODO: should probably fix this shit, but for ¯\_(ツ)_/¯ (meh)
    ktlint.ignoreFailures = true
    ktlint.outputToConsole = false
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.bundles.kotlinx.serializations)

    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)

    implementation(libs.bundles.flyway)
    implementation(libs.hikari)
    implementation(libs.bundles.exposed)
    implementation(libs.kreds)
    implementation(libs.db.h2)
    implementation(libs.db.postgresql)

    implementation(libs.bundles.kache)

    implementation(libs.bundles.arrow)
    implementation(libs.bundles.suspendapp)

    implementation(libs.kjwt)

    implementation(libs.micrometer)
    implementation(libs.logback)

    testImplementation(libs.test.ktor.server)
    testImplementation(libs.test.kotlinx.coroutines)
    testImplementation(libs.bundles.test.kotest.core)
    testImplementation(libs.bundles.test.kotest.assertions)
    testImplementation(libs.bundles.test.kotest.extensions)
    testImplementation(libs.test.mockk)

    detektPlugins(libs.detekt.formatting)
}

tasks
    .withType<KotlinCompile>()
    .configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }

tasks.register<JavaExec>("runDevelopment") {
    group = "application"
    description = "Runs the application in development mode."

    dependsOn("classes")

    mainClass.set("hr.algebra.domace.infrastructure.ApplicationKt")
    classpath = sourceSets["main"].runtimeClasspath

    jvmArgs(
        "-Dio.ktor.development=true",
        "-Dio.netty.tryReflectionSetAccessible=true"
    )
}

tasks.register("rebuild") {
    group = "build"
    description = "Rebuilds the project for fast development."

    dependsOn("classes")
}

tasks
    .withType<Detekt>()
    .configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
    }

tasks.test {
    useJUnitPlatform()
}
