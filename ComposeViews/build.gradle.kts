@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

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
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("org.jetbrains.compose")
    id("com.android.library")
    //id("maven-publish")
    id("convention.publication")
    kotlin("plugin.compose")
    id("com.vk.vkompose")
}

if (vkomposeIsCheck)
    vkompose {
        skippabilityCheck = true

        recompose {
            isHighlighterEnabled = true
            isLoggerEnabled = true
        }

        testTag {
            isApplierEnabled = true
            isDrawerEnabled = true
            isCleanerEnabled = true
        }

        sourceInformationClean = true
    }
group = "com.vickyleu.composeviews"
//group = "io.github.ltttttttttttt"
//上传到mavenCentral命令: ./gradlew publishAllPublicationsToSonatypeRepository
//mavenCentral后台: https://s01.oss.sonatype.org/#stagingRepositories
//version = "$composeVersion.1"
version = "1.0.2"

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
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
        //kotlin引入静态库参考: https://kotlinlang.org/docs/multiplatform-dsl-reference.html#cinterops
        iosTarget.compilations.all {
        }
    }

    js(IR) {
        browser()
        compilations.all {
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "ComposeViews"
        browser {
            commonWebpackConfig {
                outputFileName = "ComposeViews.js"
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

    cocoapods {
        summary = "Jatpack(JetBrains) Compose views"
        homepage = "https://github.com/ltttttttttttt/ComposeViews"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "ComposeViews"
            isStatic = true
        }
//        extraSpecAttributes["resources"] =
//            "['resources/**']"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //跨平台compose
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.animation)
                api(compose.ui)
                implementation(compose.components.resources)
                api("io.github.ltttttttttttt:DataStructure:1.1.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.4.0")
                //协程
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13")
            }
        }

        val desktopMain by getting {
            dependencies {
                //api(project(":ComposeViews")) {
                //    exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-android")//剔除安卓协程依赖
                //}
                //协程
                api("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")
            }
        }
    }
}

android {
    compileSdk = 34
    namespace = "com.lt.compose_views"
    buildFeatures.buildConfig = true
    defaultConfig {
        minSdk = 21
        sourceSets["main"].manifest.srcFile("src/main/AndroidManifest.xml")
        consumerProguardFiles("consumer-rules.pro")
    }
    lint{
        targetSdk = 31
    }

    publishing {
        singleVariant("release"){
            withJavadocJar()
            withSourcesJar()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

//compose配置
compose {
    resources{
        publicResClass = false
        packageOfResClass="io.github.ltttttttttttt.composeviews.generated.resources"
    }
    //kotlinCompilerPlugin.set("androidx.compose.compiler:compiler:$composeCompilerVersion")
}