package ru.paylab.publicapplication.project

import com.android.build.api.dsl.ApplicationExtension
import com.github.triplet.gradle.play.PlayPublisherExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.create
import java.io.FileInputStream
import java.util.Properties

class PlayPublisherPlugin : Plugin<Project> {
    companion object {
        const val SIGN_APP_CONFIG_RELEASE = "releasePublic"
        const val SIGN_APP_CONFIG_DEBUG = "debug"
        const val BUILD_APP_CONFIG_TASK = "publishApp"
    }

    override fun apply(target: Project) {

        with(target) {
            plugins.apply("com.github.triplet.play")
        }
        target.extensions.create<PlayPublisherApiExtension>(PlayPublisherApiExtension.PUBLISH_APP_CONFIG)

        signingConfig(target)

        // Build config
        buildPublishingConfig(target)

        // Setting Config Api play build
        settingApiPlay(target)
    }

    private fun settingApiPlay(targetProject: Project) {
        with(targetProject) {
            project.applicationExtension.apply {
                (this as ExtensionAware).extensions.configure<NamedDomainObjectContainer<PlayPublisherExtension>>(
                    "playConfigs"
                ) {
                    register(BUILD_APP_CONFIG_TASK) {
                        enabled.set(true)
                    }
                }
            }
        }
    }
    private fun buildPublishingConfig(targetProject: Project) {
        with(targetProject) {
            val appExtension = project.applicationExtension
            appExtension.apply {
                buildTypes.create(BUILD_APP_CONFIG_TASK).apply {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(
                        appExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                    if (appExtension.signingConfigs.findByName(SIGN_APP_CONFIG_RELEASE) != null) {
                        println("PlayPublisherPlugin: using releasePublic.jks key")
                        signingConfig = appExtension.signingConfigs.getByName(
                            SIGN_APP_CONFIG_RELEASE
                        )
                    } else {
                        println("PlayPublisherPlugin: release: using debug key")
                        signingConfig = appExtension.signingConfigs.getByName(SIGN_APP_CONFIG_DEBUG)
                    }
                }
            }
        }
    }

    private fun signingConfig(targetProject: Project) {
        with(targetProject) {
            val appExtension = project.applicationExtension
            appExtension.apply {
                val keyStorePropertyFileName =
                    System.getenv("KEY_STORE_SIGN") ?: "$rootDir/CI/keystore.properties"
                val keystorePropertiesFile = rootProject.file(keyStorePropertyFileName)
                if (keystorePropertiesFile.exists()) {
                    signingConfigs {
                        val keystoreProperties = Properties()
                        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                        register(SIGN_APP_CONFIG_RELEASE) {
                            keyAlias = keystoreProperties["keyAlias"] as String
                            keyPassword = keystoreProperties["keyPassword"] as String
                            storeFile = file(keystoreProperties["storeFile"] as String)
                            storePassword = keystoreProperties["storePassword"] as String
                        }
                    }
                }
            }
        }
    }
}

internal val Project.applicationExtension: ApplicationExtension
    get() = extensions.findByName("android") as ApplicationExtension