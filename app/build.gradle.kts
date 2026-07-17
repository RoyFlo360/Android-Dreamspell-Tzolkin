import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Signing credentials live in keystore.properties (gitignored, see art/../README).
// Absent on CI and fresh clones - release stays unsigned there rather than failing the build.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

android {
    namespace = "com.imix.dreamspell_tzolkin"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.imix.dreamspell_tzolkin"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            signingConfig = signingConfigs.getByName("release").takeIf { keystorePropsFile.exists() }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        // res/ carries redundant AppCompat/Material resources (see CLAUDE.md). Their style
        // hierarchies trip ResourceCycle and their layout-land variants trip
        // MissingDefaultResource. Library resources, not ours to fix - and lintVital blocks
        // the release build on them.
        disable += setOf("ResourceCycle", "MissingDefaultResource")
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.viewpager2)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity.ktx)
    testImplementation(libs.junit)
    // BDD (run locally: ./gradlew :app:testDebugUnitTest --tests "*RunCucumberTest")
    testImplementation(libs.cucumber.java)
    testImplementation(libs.cucumber.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}