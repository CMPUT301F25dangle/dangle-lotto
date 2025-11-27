plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.dangle_lotto"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.dangle_lotto"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.firestore)
    implementation(libs.legacy.support.v4)
    implementation(libs.espresso.idling.resource)
    implementation(libs.rules)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //implementation(files("/home/mahdlinux/Android/Sdk/platforms/android-34/android.jar"))

    //javadocs
    //implementation(files("C:/Users/adi4s/AppData/Local/Android/Sdk/platforms/android-36/android.jar"))
    //implementation(files("/home/mahdlinux/Android/Sdk/platforms/android-34/android.jar"))

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    // Firebase Auth
    implementation("com.google.firebase:firebase-auth:23.0.0")
    // Firebase functions
    implementation("com.google.firebase:firebase-functions:22.1.0")

    // firebase analytics
    implementation("com.google.firebase:firebase-analytics")

    // import dependency to generate QR code
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    // import firebase storage dependency
    implementation("com.google.firebase:firebase-storage:20.3.0")

    // image picker dependencies
    implementation("androidx.activity:activity-ktx:1.12.0")

    // image loading dependency
    implementation("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")

    // qr code scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}


apply(plugin = "com.google.gms.google-services")

