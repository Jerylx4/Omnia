plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.prograavanzada.omnia"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    buildFeatures{
        buildConfig = true
    }

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.prograavanzada.omnia"
        minSdk = 24
        targetSdk = 36
        versionCode = 20
        versionName = "2.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.17.0"))
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}