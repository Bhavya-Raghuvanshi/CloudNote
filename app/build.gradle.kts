plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

}

android {
    namespace = "com.example.cloudnote"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cloudnote"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Libraries
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    implementation("com.airbnb.android:lottie:6.2.0")
}