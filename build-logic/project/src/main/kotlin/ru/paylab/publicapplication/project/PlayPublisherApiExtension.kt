package ru.paylab.publicapplication.project

import com.github.triplet.gradle.play.PlayPublisherExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

//ANDROID_PUBLISHER_CREDENTIALS
open class PlayPublisherApiExtension(
    private val project: Project,
) {

    companion object {
        const val PUBLISH_APP_CONFIG = "publishAppConfig"
    }

    private var publishSettings: UploadPublishAppSettings? = null

    fun publishSettings(action: UploadPublishAppSettings.() -> Unit) {
        if (publishSettings == null) {
            publishSettings = UploadPublishAppSettings()
        }

        // Load init action
        publishSettings?.run(action)

        // Set publish config
        uploadConfig(publishSettings)
    }

    private fun uploadConfig(uploadPlayPublisherSettings: UploadPublishAppSettings?) {
        with(project) {
            extensions.configure<PlayPublisherExtension> {
                uploadPlayPublisherSettings?.let {
                    enabled.set(false) // Default only for Build type: "releasePublic"
                    track.set(uploadPlayPublisherSettings.track)
                    userFraction.set(uploadPlayPublisherSettings.userFraction) // 10%
                    defaultToAppBundles.set(true)
                    uploadPlayPublisherSettings.serviceAccountCredentials?.let {
                        serviceAccountCredentials.set(file(it))
                    }
                }
            }
        }
    }
}
