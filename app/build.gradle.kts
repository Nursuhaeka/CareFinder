plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Plugin for Google Services
}

android {
    namespace = "com.example.carefinder"
    compileSdk = 35 // Ensure your compileSdk version matches your installed SDK

    defaultConfig {
        applicationId = "com.example.carefinder"
        minSdk = 24 // Minimum SDK version supported
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // Firebase Platform (Firebase BOM ensures all Firebase dependencies are compatible)
    implementation ("commons-io:commons-io:2.11.0")
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    // Firebase Services
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth") // Firebase Authentication
    implementation("com.google.firebase:firebase-database") // Firebase Realtime Database (if needed)

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1") // Location APIs

    // AndroidX Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.coordinatorlayout)


    // Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
