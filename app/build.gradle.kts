plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "sg.edu.np.mad.fitnessultimate"
    compileSdk = 34

    defaultConfig {
        applicationId = "sg.edu.np.mad.fitnessultimate"
        minSdk = 33
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.gson)
    implementation(libs.roundedimageview)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.android.gms:play-services-auth:20.2.0")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    // for bar chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // ai things
    implementation("org.tensorflow:tensorflow-lite:2.6.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.2.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.9.0")

    //google map api
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.libraries.places:places:2.6.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.libraries.places:places:3.0.0")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")

}