import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("kotlin-kapt")
}



android {
    namespace = "com.example.wasmapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wasmapplication"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

//        val localProperties =  Properties()
//        val localPropertiesFile = rootProject.file("local.properties")
//        if (localPropertiesFile.exists()) {
//            localProperties.load(FileInputStream(localPropertiesFile))
//        }
//
//        // قراءة المفتاح WASM_API_KEY
//        val wasmApsApiKey = localProperties.getProperty("WASM_API_KEY")
//        if (wasmApsApiKey != null) {
//            buildConfigField("String", "WASM_API_KEY", "\"${wasmApsApiKey}\"")
//        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
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
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.vision.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //lifecycle.viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    //Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
//    implementation(libs.hilt.lifecycle.viewmodel)
//    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


    //HTTP
    implementation(libs.squareup.retrofit2)
    implementation(libs.squareup.retrofit2.converter.gson)
    //gemini
    implementation(libs.generativeai)
    //media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.exoplayer.ui)


}
kapt {
    correctErrorTypes = true
}