val javaLangVersion = "${JavaVersion.VERSION_1_8}"
val libName = rootProject.name

val publicationName = "mavenJava"
val publishToCentral = true
val isRelease = true

group = "com.github.gorttar"
version = "1.0.0${"".takeIf { isRelease } ?: "-SNAPSHOT"}"

plugins {
    java
    id("idea")
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    `java-library`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("test"))
    implementation(group = "com.willowtreeapps.assertk", name = "assertk-jvm", version = "0.20")

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.6.2")
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.6.2")
    implementation(kotlin("stdlib-jdk8"))
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.6.2")
        classpath(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version = "1.3.72")
    }
}

tasks {
    test { useJUnitPlatform() }

    listOf(compileJava, compileTestJava).forEach {
        it {
            sourceCompatibility = javaLangVersion
            targetCompatibility = javaLangVersion
        }
    }

    listOf(compileKotlin, compileTestKotlin).forEach {
        it {
            kotlinOptions {
                jvmTarget = javaLangVersion
            }
        }
    }

    javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }

    wrapper { gradleVersion = "6.5.1" }
}

val snapshotUrl = "https://oss.sonatype.org/content/repositories/snapshots"
    .takeIf { publishToCentral }
    ?: "$buildDir/repos/snapshots"
val releaseUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    .takeIf { publishToCentral }
    ?: "$buildDir/repos/releases"

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            groupId = "$group"
            artifactId = libName
            from(components["java"])
            pom {
                name.set("Handy libraries")
                description.set("Bunch of somewhat usable libraries without any particular direction")
                url.set("https://github.com/gorttar/$libName")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/gorttar/$libName/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("gorttar")
                        name.set("Andrey Antipov")
                        email.set("gorttar@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/gorttar/$libName.git")
                    developerConnection.set("scm:git:git://github.com/gorttar/$libName.git")
                    url.set("https://github.com/gorttar/$libName")
                }
            }
        }
    }
    repositories {
        maven {
            url = (releaseUrl.takeIf { isRelease } ?: snapshotUrl).let(::uri)
            publishToCentral.takeIf { it }?.also {
                repositories {
                    val nexusUsername: String by project
                    val nexusPassword: String by project

                    credentials {
                        username = nexusUsername
                        password = nexusPassword
                    }
                }
            }
        }
    }
}

signing { sign(publishing.publications[publicationName]) }