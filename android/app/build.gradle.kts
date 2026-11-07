plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val vCode = project.findProperty("versionCode")?.toString()?.toIntOrNull() ?: 1
val vName = project.findProperty("versionName")?.toString() ?: "1.0.$vCode"

android {
    namespace = "com.rahatsharif.dalilapp"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = when {
                file("${rootDir}/release.keystore").exists() -> file("${rootDir}/release.keystore")
                file("${rootDir}/android/release.keystore").exists() -> file("${rootDir}/android/release.keystore")
                else -> file("${projectDir}/release.keystore")
            }
            storePassword = "dalilapp123"
            keyAlias = "dalilkey"
            keyPassword = "dalilapp123"
        }
    }

    defaultConfig {
        applicationId = "com.rahatsharif.dalilapp"
        minSdk = 24
        targetSdk = 35
        versionCode = vCode
        versionName = vName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.webkit)
    implementation(libs.google.gson)
}
