import com.android.kotlin.multiplatform.ide.models.serialization.androidTargetKey

/*
 * Copyright lt 2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}
kotlin{
    jvmToolchain(17)
    androidTarget()
}
android {
    compileSdk = 34
    namespace = "com.lt.test_compose"
    buildFeatures.buildConfig = true
    defaultConfig {
        applicationId = "com.lt.test_compose"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    //签名设置
    signingConfigs {
        create("release") {
            //设置debug时用的签名和密码
            storeFile = File("${project.rootDir.absolutePath}/composeview.jks")
            storePassword = "11111111"//签名密码
            keyAlias = "key"//别名
            keyPassword = "11111111"//别名密码
            enableV2Signing = true//启用v2签名
            enableV1Signing = true//启用v1签名
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//        freeCompilerArgs = listOf("-Xallow-kotlin-package")
//    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        //kotlinCompilerExtensionVersion = composeCompilerVersion
    }
}

dependencies {
    implementation(project(":common_app"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.coil.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.constraintlayout.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}