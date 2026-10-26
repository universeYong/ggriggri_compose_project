plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.ahn.ggriggri"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
//        buildConfig = true
    }
}
kotlin {
    jvmToolchain(21)
}


dependencies {

    implementation(project(":feature:common_ui"))
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.bundles.work.libraries)


    // Compose Preview를 위해 아래 두 의존성을 추가합니다.
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // APNG 라이브러리

    implementation(libs.bundles.apng.libraries)


    implementation(libs.bundles.kakao.libraries)
    implementation(libs.bundles.navigation.libraries)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // Hilt Navigation (ViewModel 주입에 필요)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    // @HiltWorker(DailyQuestionWorker)의 팩토리를 생성한다. 없으면 HiltWorkerFactory가 Worker를 만들지 못한다.
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.compose)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}