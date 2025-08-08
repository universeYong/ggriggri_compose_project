plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    namespace = "com.ahn.ggrigggri"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ahn.ggrigggri"
        minSdk = 24
        //noinspection EditedTargetSdkVersion
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
kotlin {
    jvmToolchain(21)
}
secrets {
// 사용할 프로퍼티 파일의 이름을 선언(선언하지 않으면: "local.properties")
    propertiesFileName = "secret.properties"
// CI/CD 환경을 위한 기본 프로퍼티 파일을 지정
    // 이 파일은 버전 관리에 포함될 수 있음
    //defaultPropertiesFileName = "secrets.defaults.properties"
// Secrets Plug-In 무시할 키의 목록을 정규 표현식으로 지정가능
    // "sdk.dir"은 기본적으로 무시
    //ignoreList.add("temp_internal_key") // "temp_internal_key" 키 무시
    //ignoreList.add("debug.*") // "debug"로 시작하는 모든 키 무시
}


dependencies {
    implementation(project(":feature:ggriggri_ui"))
    implementation(project(":feature:common_ui"))


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.firebase.firestore.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.v2.user)
    implementation(libs.bundles.coil.libraries)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}