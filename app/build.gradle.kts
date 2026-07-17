plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.imix.dreamspell_tzolkin"
    compileSdk {
        version = release(34)
    }

    defaultConfig {
        applicationId = "com.imix.dreamspell_tzolkin"
        minSdk = 26
        targetSdk = 34
        versionCode = 31
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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