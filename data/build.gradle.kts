plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // google-services 플러그인은 google-services.json이 있는 :app에만 적용한다.
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.hilt.gradle)
}

android {
    namespace = "com.ahn.data"
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
}
kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase.libraries)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // SerializationModule의 KotlinJsonAdapterFactory(리플렉션)로 동작하므로 codegen(ksp)은 걸지 않는다.
    implementation(libs.bundles.moshi.libraries)
    implementation(libs.bundles.datastore.libraries)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}