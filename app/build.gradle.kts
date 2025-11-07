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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // javadocs
    implementation(files("C:/Users/adi4s/AppData/Local/Android/Sdk/platforms/android-36/android.jar"))

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    // Firebase Auth
    implementation("com.google.firebase:firebase-auth:23.0.0")


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")



    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries// Import the Firebase BoM
    //  implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    //
    //
    //  // TODO: Add the dependencies for Firebase products you want to use
    //  // When using the BoM, don't specify versions in Firebase dependencies
    //  implementation("com.google.firebase:firebase-analytics")
    //
    //
    //  // Add the dependencies for any other desired Firebase products
    //  // https://firebase.google.com/docs/android/setup#available-libraries
}

apply(plugin = "com.google.gms.google-services")
