plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.modpract"
    compileSdk = 35

    // Add this to handle resource processing issues
    aaptOptions {
        noCompress("png", "jpg")
        var cruncherEnabled = false
        additionalParameters("--warn-manifest-validation", "--no-version-vectors")
    }


    defaultConfig {
        applicationId = "com.example.modpract"
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
        debug {
            // No special configurations for debug
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // Add this to handle resource merging issues
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    
    // Add this to handle resource processing issues
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)


    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    
    // OpenStreetMap dependency
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation(libs.firebase.firestore.ktx)

    // Preference dependency for PreferenceManager
    implementation("androidx.preference:preference-ktx:1.2.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
    // Explicitly exclude Firebase to avoid conflicts
    configurations.all {
        exclude(group = "com.google.firebase", module = "firebase-firestore")
    }
}

// Add a task to clean the build directory
tasks.register("cleanBuildDir") {
    doLast {
        delete(buildDir)
    }
}

// Make the clean task depend on cleanBuildDir
tasks.named("clean") {
    dependsOn("cleanBuildDir")
}