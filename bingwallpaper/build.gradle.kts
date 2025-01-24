import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "tv.projectivy.plugin.wallpaperprovider.bingwallpaper"
    compileSdk = 35

    defaultConfig {
        applicationId = "tv.projectivy.plugin.wallpaperprovider.bingwallpaper"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.00"

        base {
            archivesName = "ProjectivyPlugin-BingWallpaper-$versionName-c$versionCode"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    signingConfigs {
        create("genericRelease") {
            keyAlias = gradleLocalProperties(rootDir, providers).getProperty("GENERIC_KEY_ALIAS")
            keyPassword = gradleLocalProperties(rootDir, providers).getProperty("GENERIC_KEY_PASSWORD")
            storeFile = file(gradleLocalProperties(rootDir, providers).getProperty("GENERIC_STORE_FILE"))
            storePassword = gradleLocalProperties(rootDir, providers).getProperty("GENERIC_STORE_PASSWORD")
        }
        create("playstoreRelease") {
            keyAlias = gradleLocalProperties(rootDir, providers).getProperty("PLAYSTORE_KEY_ALIAS")
            keyPassword = gradleLocalProperties(rootDir, providers).getProperty("PLAYSTORE_KEY_PASSWORD")
            storeFile = file(gradleLocalProperties(rootDir, providers).getProperty("PLAYSTORE_STORE_FILE"))
            storePassword = gradleLocalProperties(rootDir, providers).getProperty("PLAYSTORE_STORE_PASSWORD")
        }
    }

    flavorDimensions.add("store")
    productFlavors {
        create("playstore") {
            dimension = "store"
        }
        create("generic") {
            dimension = "store"
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-dev"
        }
        release {
            productFlavors.forEach { flavor ->
                if (flavor.name.contains("playstore")) {
                    flavor.signingConfig = signingConfigs.getByName("playstoreRelease")
                } else  {
                    flavor.signingConfig = signingConfigs.getByName("genericRelease")
                }
            }
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.leanback:leanback:1.2.0-alpha04")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    implementation("com.google.code.gson:gson:2.11.0")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    implementation(project(":api"))
}