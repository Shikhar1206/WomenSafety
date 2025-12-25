plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.womensafety"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.womensafety"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    kotlinOptions {
        jvmTarget = "17"
    }
}


dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")


// Location
    implementation("com.google.android.gms:play-services-location:21.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

//plugins {
//    id("com.android.application")
//    id("org.jetbrains.kotlin.android")
//    id("kotlin-kapt")
//}
//
//android {
//    namespace = "com.example.womensafety"
//    compileSdk = 34
//
//    defaultConfig {
//        applicationId = "com.example.womensafety"
//        minSdk = 24
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//        }
//    }
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//}
//
////kotlinOptions {
////    jvmTarget = "17"
////}
//
//dependencies {
//
//    implementation("androidx.core:core-ktx:1.13.1")
//    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
//
//    // ROOM (ONLY THESE)
//    implementation("androidx.room:room-runtime:2.6.1")
//    kapt("androidx.room:room-compiler:2.6.1")
//
//    // COROUTINES
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
//}
