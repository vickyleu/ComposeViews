@file:OptIn(ExperimentalKotlinGradlePluginApi::class,
    org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class
)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

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
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.cocoapods)
//
//    kotlin("multiplatform")
//    id("org.jetbrains.compose")
//    id("com.android.library")
//    id("com.google.devtools.ksp")
//    kotlin("native.cocoapods")
//    kotlin("plugin.compose")
//    id("com.vk.vkompose")
}

//if (vkomposeIsCheck)
//    vkompose {
//        skippabilityCheck = true
//
//        recompose {
//            isHighlighterEnabled = true
//            isLoggerEnabled = true
//        }
//
//        testTag {
//            isApplierEnabled = true
//            isDrawerEnabled = true
//            isCleanerEnabled = true
//        }
//
//        sourceInformationClean = true
//    }

group = "com.vickyleu.composeviews"
//group = "com.lt.ltttttttttttt"
version = "1.0.3"
android {
    compileSdk = 33
    namespace = "com.lt.common_app"
    buildFeatures.buildConfig = true
    defaultConfig {
        minSdk = 21
        var testIndex = "-1"
        try {
            testIndex = File(project.rootDir, "test_index.txt").readText()
        } catch (ignore: Exception) {
        }
        buildConfigField("int", "TEST_INDEX", testIndex)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        targetSdk = 31
        checkDependencies = true//开启 lint 性能优化
        abortOnError = false//忽略Lint检查
        checkReleaseBuilds = false//压制警告,打包的时候有时候会有莫名其妙的警告
    }
}
compose {
    resources{
        publicResClass = false
        packageOfResClass="io.github.ltttttttttttt.composeviews.generated.resources"
    }
}
kotlin {
    applyDefaultHierarchyTemplate()
    jvmToolchain(17)
    androidTarget {
        compilerOptions{
            jvmTarget.set(JvmTarget.fromTarget("17"))
        }
    }

    jvm("desktop") {
        compilerOptions{
            jvmTarget.set(JvmTarget.fromTarget("17"))
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries {
        }
    }

    js(IR) {
        browser()
    }

    wasmJs {
        moduleName = "common_app"
        browser {
            commonWebpackConfig {
                outputFileName = "common_app.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                    }
                }
            }
        }
        binaries.executable()
    }

//    macosX64 {
//        binaries {
//            executable {
//                entryPoint = "main"
//            }
//        }
//    }
//    macosArm64 {
//        binaries {
//            executable {
//                entryPoint = "main"
//            }
//        }
//    }
    cocoapods {
        version = "0.0.1"
        summary = "Jatpack(JetBrains) Compose views"
        homepage = "https://github.com/ltttttttttttt/ComposeViews"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "common_app"
            isStatic = true
        }
//        extraSpecAttributes["resources"] =
//            "['../ComposeViews/resources/**', '../desktop_app/src/desktopMain/resources/**']"
    }
    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                api(project(":views"))
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.animation)
                api(compose.ui)
                api(libs.coil.compose)//coil图片加载
                api(libs.coil.network.ktor)//图片网络请求引擎
                implementation(compose.components.resources)//api不能生成Res?
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.appcompat)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }

        val desktopMain by getting {
            dependencies {
                api(compose.preview)
            }
        }
        val iosMain by getting {
            kotlin.srcDir("build/generated/ksp/ios/iosMain/kotlin")
            dependencies {
            }
        }

        val jsMain by getting {
            dependencies {
            }
        }
    }
    ksp {
        arg("packageListWithVirtualReflection", "com.lt.common_app")
    }
}

dependencies {
    add("kspCommonMainMetadata", "io.github.ltttttttttttt:VirtualReflection:1.3.1")
}