plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.taskchecker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.taskchecker"
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
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.squareup.retrofit2:retrofit:2.3.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.3.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("androidx.compose.ui:ui:1.6.7")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("androidx.compose.foundation:foundation:1.6.7")

    implementation ("androidx.compose.runtime:runtime:1.6.7")
    implementation ("androidx.compose.ui:ui-tooling:1.6.7")
    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation ("org.apache.commons:commons-text:1.9")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}