plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val vCode = project.findProperty("versionCode")?.toString()?.toIntOrNull() ?: 2
val vName = project.findProperty("versionName")?.toString() ?: "1.0.2"

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

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.webkit)
}
