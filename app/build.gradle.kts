plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sinhvien.livescore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sinhvien.livescore"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation (libs.appcompat)
    implementation (libs.threetenabp)
    implementation(platform(libs.firebase.bom))
    implementation (libs.google.firebase.auth)
    implementation (libs.google.firebase.firestore)
    implementation(libs.google.firebase.database)
    implementation(libs.firebase.storage)


    // Android dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.v1101)
    implementation(libs.constraintlayout.v221)
    implementation (libs.cardview)
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("androidx.core:core-splashscreen:1.0.1")
    // Networking
    implementation(libs.volley)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)


    implementation (libs.firebase.storage)
    // Glide for image loading
    implementation(libs.glide)
    implementation(libs.swiperefreshlayout)
    implementation(libs.core.splashscreen)
    annotationProcessor(libs.compiler)

    // Navigation Component
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
