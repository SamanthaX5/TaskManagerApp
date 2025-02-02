plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.taskmanagerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.taskmanagerapp"
        minSdk = 24
        targetSdk = 34
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

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Core Android Libraries
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat.v170)
    implementation(libs.material.v1120)

    // Firebase Libraries
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.database.ktx)

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Google API Client Libraries (Including Calendar API)
    implementation("com.google.api-client:google-api-client:1.31.2")
    implementation("com.google.api-client:google-api-client-android:1.31.2")
    implementation("com.google.api-client:google-api-client-gson:1.31.2")
    implementation("com.google.http-client:google-http-client-gson:1.42.2")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.17.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev305-1.32.1") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }

    // Networking (Retrofit for API Calls)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}





