plugins {
    kotlin("jvm")
}

group = "com.github.gorttar"
version = "2.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    testImplementation(project(":testing"))
    testImplementation(kotlin("test"))
    testImplementation(group = "com.willowtreeapps.assertk", name = "assertk", version = "0.28.1")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(21)
}