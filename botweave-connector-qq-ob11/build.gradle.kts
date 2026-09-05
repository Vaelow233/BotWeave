plugins {
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "8.3.11"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":botweave-core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}