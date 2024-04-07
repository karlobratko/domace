import io.gitlab.arturbosch.detekt.Detekt

val kotlinVersion: String by project
val logbackVersion: String by project
val coroutinesVersion: String by project
val ktorVersion: String by project
val flywayVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val h2Version: String by project
val postgresqlVersion: String by project
val arrowVersion: String by project
val suspendappVersion: String by project
val kjwtVersion: String by project
val serializationVersion: String by project
val datetimeVersion: String by project
val kacheVersion: String by project
val kredsVersion: String by project
val kotestVersion: String by project
val kotestKtorVersion: String by project
val kotestArrowVersion: String by project
val mockkVersion: String by project
val micrometerVersion: String by project
val detektVersion: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.9"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
}

group = "hr.algebra"
version = "0.0.1"

application {
    mainClass.set("hr.algebra.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // IMPLEMENTATION

    // kotlinx.coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // ktor
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-call-id-jvm")
    implementation("io.ktor:ktor-server-http-redirect-jvm")
    implementation("io.ktor:ktor-server-hsts-jvm")
    implementation("io.ktor:ktor-server-forwarded-header-jvm")
    implementation("io.ktor:ktor-server-default-headers-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-compression-jvm")
    implementation("io.ktor:ktor-server-double-receive-jvm")
    implementation("io.ktor:ktor-server-auto-head-response-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-apache-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    // flyway
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    // exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")

    // hikari
    implementation("com.zaxxer:HikariCP:$hikariVersion")

    // h2
    implementation("com.h2database:h2:$h2Version")

    // postgresql
    implementation("org.postgresql:postgresql:$postgresqlVersion")

    // arrow
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-core-serialization:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines-jvm:$arrowVersion")

    // suspendapp
    implementation("io.arrow-kt:suspendapp:$suspendappVersion")
    implementation("io.arrow-kt:suspendapp-ktor:$suspendappVersion")

    // kjwt
    implementation("io.github.nefilim.kjwt:kjwt-core:$kjwtVersion")

    // kotlinx.serializations
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:$serializationVersion")

    // kotlinx.datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")

    // kache
    implementation("com.mayakapps.kache:kache:$kacheVersion")
    implementation("com.mayakapps.kache:file-kache:$kacheVersion")

    // kreds
    implementation("io.github.crackthecodeabhi:kreds:$kredsVersion")

    // micrometer
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    // logback
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // TEST-IMPLEMENTATION

    // kotlin test
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // ktor test
    testImplementation("io.ktor:ktor-server-tests-jvm")

    // kotlinx.coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    // kotest - framework
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")

    // kotest - property testing
    testImplementation("io.kotest:kotest-property:$kotestVersion")

    // kotest - assertions
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-sql:$kotestVersion")

    // kotest - extensions
    testImplementation("io.kotest:kotest-extensions-now:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-jvm:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-ktor:$kotestKtorVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$kotestArrowVersion")

    // mockk
    testImplementation("io.mockk:mockk:$mockkVersion")

    // PLUGINS
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:$detektVersion")
}

detekt {
    toolVersion = detektVersion
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}
