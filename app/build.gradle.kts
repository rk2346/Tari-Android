import com.android.tools.r8.internal.im
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {

    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
    kotlin("kapt")
}

/* ------------------------------------------------------------------ */

android {
    namespace = "com.example.tarimobileas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tarimobileas"
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

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("11")
        }


        buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0"
    }
}


/* ------------------------------------------------------------------ */

    dependencies {
        // --- AndroidX core -------------------------------------------------
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)

        // --- Jetpack Compose ------------------------------------------------

        implementation(platform("androidx.compose:compose-bom:2026.01.00"))

        // Core UI libraries
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-graphics")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3")

        // Needed for setContent { … } inside a ComponentActivity
        implementation(libs.androidx.activity.compose)

        // --- OkHttp ---------------------------------------------------------
        implementation("com.squareup.okhttp3:okhttp:5.3.2")

        // --- Kotlin serialization -------------------------------------------
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

        //
        implementation("com.jakewharton.threetenabp:threetenabp:1.4.9")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
        implementation("androidx.room:room-runtime:2.8.4")
        kapt("androidx.room:room-compiler:2.8.4")
        implementation("androidx.room:room-ktx:2.8.4")


        implementation("androidx.navigation:navigation-compose:2.9.6")

        implementation("androidx.compose.material3:material3:1.4.0")
        implementation("androidx.compose.material:material-icons-extended:1.7.8")

        // --- Test / AndroidTest -------------------
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)

        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.test.manifest)
                }
        }

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.3.0")
    }
}
