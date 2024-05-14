import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    // kotlin多平台插件
    alias(libs.plugins.kotlinMultiplatform)
    // android应用插件
    alias(libs.plugins.androidApplication)
    // jetbrains compose插件
    alias(libs.plugins.jetbrainsCompose)

    alias(libs.plugins.cocoapods)

    alias(libs.plugins.compose.compiler)
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
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                        add(project.projectDir.path + "/commonMain/")
                        add(project.projectDir.path + "/wasmJsMain/")
                    }
                }
            }
        }
        binaries.executable()
    }
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.jvmTarget.get()
            }
        }
    }
    js(IR) {
        browser()
        binaries.executable()
        compilations.all {
            defaultSourceSet.resources.srcDirs("../ComposeViews/resources",
                "../desktop_app/src/desktopMain/resources")
        }
    }
    jvm("desktop"){
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jvmTarget.get()
        }
//        withJava() //https://issuetracker.google.com/issues/248593403?hl=ko
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.apply {
            framework {
                baseName = "sample"
                isStatic = true
                // https://youtrack.jetbrains.com/issue/KT-56152/KMM-Cannot-infer-a-bundle-ID-from-packages-of-source-files-and-exported-dependencies#focus=Comments-27-6806555.0-0
                binaryOption("bundleId", "com.lt.test_compose")
            }
        }
    }

    cocoapods {
        version = "0.0.1"
        summary = "Jatpack(JetBrains) Compose views"
        homepage = "https://github.com/ltttttttttttt/ComposeViews"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "sample"
            isStatic = true
        }
        /*this.pod("CompViews") {
            this.path(file("../CompViews"))
        }*/
//        extraSpecAttributes["resources"] =
//            "['../ComposeViews/resources/**', '../desktop_app/src/desktopMain/resources/**', 'src/iosMain/resources/**']"
    }
    sourceSets{
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
                // Required for CPointer etc. since Kotlin 1.9.
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                optIn("androidx.compose.runtime.ExperimentalComposeApi")

            }
        }
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(project.dependencies.platform(libs.coroutines.bom))
            implementation(project.dependencies.platform(libs.kotlin.bom))
        }
        androidMain.dependencies {
        }
        jsMain.dependencies {
            implementation(projects.commonApp)
        }
        val desktopMain by getting {
            dependencies {
                implementation(projects.commonApp) {
                    exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-android")//剔除安卓协程依赖
                }
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        var testIndex = "-1"
        try {
            testIndex = File(project.rootDir, "test_index.txt").readText()
        } catch (ignore: Exception) {
        }
        args += listOf(testIndex)
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jvm"
            packageVersion = "1.0.0"
        }
    }
}

compose.experimental {
    web.application {}
}
android{

    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "com.lt.test_compose"
    sourceSets["main"].apply {
        manifest {
            srcFile("src/androidMain/AndroidManifest.xml")
        }
        res {
            srcDirs("src/androidMain/res")
        }
        resources {
            srcDirs("src/commonMain/resources")
        }
    }

    defaultConfig {
        applicationId = "com.lt.test_compose"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
        getByName("release") {
//            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    lint {
        checkDependencies = true//开启 lint 性能优化
        abortOnError = false//忽略Lint检查
        checkReleaseBuilds = false//压制警告,打包的时候有时候会有莫名其妙的警告
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    dependencies {
        implementation(projects.commonApp)
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
}