// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.secrets.gradle.plugin) apply false
    alias(libs.plugins.hilt.gradle) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.ksp) apply false
}

// 데몬은 file.encoding=COMPAT(gradle.properties 참고)이지만 테스트 JVM의 기본 문자셋은 UTF-8로 고정한다.
subprojects {
    tasks.withType<Test>().configureEach {
        defaultCharacterEncoding = "UTF-8"
    }
}
