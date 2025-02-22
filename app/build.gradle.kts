plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.exner.tools.activitytimercompanion"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.exner.tools.activitytimercompanion"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            vcsInfo.include = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildToolsVersion = "35.0.0"

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName = "Activity_Timer_Companion_${variant.baseName}_${variant.versionName}_${variant.versionCode}.apk"
                println("OutputFileName: $outputFileName")
                output.outputFileName = outputFileName
            }
    }
}

kotlin {
    jvmToolchain(17)

    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.livedata.core)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.runtime.livedata)

    // hilt
    implementation(libs.hilt.android)
    implementation(libs.play.services.nearby)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // compose destinations
    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)

    // Room components
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // Async image loading
    implementation(libs.coil.compose)

    // Retrofit / Moshi
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    // others
    implementation(libs.google.material)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.accompanist.permissions)
}