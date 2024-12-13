import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "ru.paylab.publicapplication.project"

private val projectJavaVersion: JavaVersion = JavaVersion.toVersion(17)

java {
    sourceCompatibility = projectJavaVersion
    targetCompatibility = projectJavaVersion
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(projectJavaVersion.toString()))
}

dependencies {
    implementation(libs.play.publisher)
    compileOnly(libs.agp.build) // Compile only to not force a specific AGP version
    compileOnly(libs.agp.common)
}

gradlePlugin {
    plugins {
        register("play.publisher.plugin") {
            id = "play.publisher.plugin"
            implementationClass = "ru.paylab.publicapplication.project.PlayPublisherPlugin"
        }
    }
}

