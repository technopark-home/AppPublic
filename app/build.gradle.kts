plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("play.publisher.plugin")
    id("ru.cian.rustore-publish-gradle-plugin") version "0.5.1"
}

val publicRuStore = "publicRuStore"
val publishGoogleCredentialsFileName = System.getenv("GOOGLE_CREDENTIALS_RELEASE") ?: "$rootDir/CI/google-play.json"
val publishRuStoreCredentialsFileName = System.getenv("RUSTORE_CREDENTIALS_RELEASE") ?: "$rootDir/CI/rustore-credentials-release.json"

publishAppConfig {
    publishSettings {
        track = "internal"
        userFraction = 0.10
        serviceAccountCredentials = publishGoogleCredentialsFileName
    }
}

android {
    namespace = "ru.paylab.publicapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.paylab.publicapplication"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        register(publicRuStore) {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (signingConfigs.findByName("releasePublic") != null) {
                println("publicRuStore: using releasePublic.jks key")
                signingConfig = signingConfigs.getByName("releasePublic")
            } else {
                println("publicRuStore: release: using debug key")
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

rustorePublish {
    if(!rootProject.file(publishRuStoreCredentialsFileName).exists()) {
        println( "$publishRuStoreCredentialsFileName not found" )
        //error("$publishRuStoreCredentialsFileName not found")
    }
    instances {
        register(publicRuStore) {
            credentialsPath = publishRuStoreCredentialsFileName
            buildFormat = ru.cian.rustore.publish.BuildFormat.APK
            publishType = ru.cian.rustore.publish.PublishType.MANUAL
            releaseNotes = listOf(
                ru.cian.rustore.publish.ReleaseNote(
                    lang = "ru-RU",
                    filePath = "$rootDir/CI/release-notes-ru.txt"
                ),
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}