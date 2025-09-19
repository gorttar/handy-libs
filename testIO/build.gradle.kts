val jUnitVersion = "5.13.4"

plugins {
    kotlin("jvm")
}

group = "com.github.gorttar"
version = "2.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":data"))
    implementation(project(":control"))
    implementation(group = "com.willowtreeapps.assertk", name = "assertk", version = "0.28.1")
    implementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = jUnitVersion)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}