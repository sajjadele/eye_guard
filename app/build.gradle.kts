import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.eyeguard"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.sajjadele.eyeguard"
        minSdk = 24
        targetSdk = 34
        versionCode = 6
        versionName = "1.4.0"
        buildConfigField("boolean", "DEBUG_TEST_INTERVAL", "false")
    }

    val localProps = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            FileInputStream(localPropertiesFile).use { load(it) }
        }
    }

    val keystorePath = System.getenv("KEYSTORE_FILE")
        ?: localProps.getProperty("release.keystore.file")
        ?: "${System.getProperty("user.home")}/.eye_guard/release.jks"
    val releaseKeystoreFile = file(keystorePath)

    val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
        ?: localProps.getProperty("release.keystore.password")
    val keyAlias = System.getenv("KEY_ALIAS")
        ?: localProps.getProperty("release.key.alias")
        ?: "eyeguard"
    val keyPassword = System.getenv("KEY_PASSWORD")
        ?: localProps.getProperty("release.key.password")
        ?: keystorePassword

    signingConfigs {
        create("release") {
            if (releaseKeystoreFile.exists() && !keystorePassword.isNullOrBlank()) {
                storeFile = releaseKeystoreFile
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            } else {
                initWith(getByName("debug"))
            }
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "DEBUG_TEST_INTERVAL", "true")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")

    implementation("androidx.activity:activity-compose:1.9.0")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-service:2.8.2")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
