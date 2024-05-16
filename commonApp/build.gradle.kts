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
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.cocoapods)
    alias(libs.plugins.compose.compiler)
}

group = "com.lt.ltttttttttttt"
compose {
    kotlinCompilerPlugin = "org.jetbrains.kotlin:kotlin-compose-compiler-plugin-embeddable:${libs.versions.kotlin.get()}"
}
android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildFeatures.buildConfig = true
    namespace = "com.lt.common_app"
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        var testIndex = "-1"
        try {
            testIndex = File(project.rootDir, "test_index.txt").readText()
        } catch (ignore: Exception) {
        }
        buildConfigField("int", "TEST_INDEX", testIndex)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    lint {
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        checkDependencies = true//开启 lint 性能优化
        abortOnError = false//忽略Lint检查
        checkReleaseBuilds = false//压制警告,打包的时候有时候会有莫名其妙的警告
    }
}

kotlin {
    @Suppress("OPT_IN_USAGE")
    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xexpect-actual-classes", // remove warnings for expect classes
            "-Xskip-prerelease-check",
            "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
        )
    }
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.jvmTarget.get()
            }
        }
    }
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.jvmTarget.get()
            }
        }
    }
    applyDefaultHierarchyTemplate()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    js(IR) {
        browser()
    }
    cocoapods {
        version = "0.0.1"
        summary = "Jetpack(JetBrains) Compose views"
        homepage = "https://github.com/ltttttttttttt/ComposeViews"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "ComposeViews"
            isStatic = true
        }
//        extraSpecAttributes["resources"] =
//            "['../ComposeViews/resources/**', '../desktop_app/src/desktopMain/resources/**']"
    }
    sourceSets {
        commonMain.get().apply {
            kotlin.srcDir("${project.layout.buildDirectory.get().asFile.relativeTo(projectDir)}/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                api(projects.compviews)
                implementation(project.dependencies.platform(libs.compose.bom))
                implementation(project.dependencies.platform(libs.coroutines.bom))
                implementation(project.dependencies.platform(libs.kotlin.bom))
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.animation)
                api(compose.ui)
                api(libs.virtual.reflection.lib)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.coil.compose)
        }

        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                //desktop图片加载器
                api(libs.load.the.image)
            }
        }
        val desktopTest by getting

        iosMain.dependencies {

        }

    }
}

ksp {
    arg("packageListWithVirtualReflection", "com.lt.common_app")
}

dependencies {
    // https://mvnrepository.com/artifact/io.github.ltttttttttttt/VirtualReflection
//    implementation("io.github.ltttttttttttt:VirtualReflection:1.2.1")

    add("kspCommonMainMetadata", "io.github.ltttttttttttt:VirtualReflection:1.2.1")
//    add("kspCommonMainMetadata", libs.virtual.reflection.ksp.get().let {
//        "${it.module}:${it.version}"
//    })
}

