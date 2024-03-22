import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Properties

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
    alias(libs.plugins.cocoapods)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.dokka)
    id("maven-publish")
//    id("convention.publication")
}



kotlin {
    androidTarget {
        publishLibraryVariants("debug", "release")
    }

    jvm("desktop") {
        compilations.all {
            defaultSourceSet.resources.srcDir("resources")
            kotlinOptions {
                jvmTarget = libs.versions.jvmTarget.get()
            }
        }
    }

    applyDefaultHierarchyTemplate()
    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.compilations.all {
            defaultSourceSet.resources.srcDir("resources")
        }
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

    js(IR) {
        browser()
        compilations.all {
            defaultSourceSet.resources.srcDir("resources")
        }
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
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(project.dependencies.platform(libs.coroutines.bom))
            implementation(project.dependencies.platform(libs.kotlin.bom))
            //跨平台compose
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.animation)
            api(compose.ui)
            api(projects.dataStructure.dataStruct)
        }

        androidMain.dependencies {
            api(libs.androidx.activity.compose)
            //协程
            api(libs.kotlinx.coroutines.android)
        }

        val desktopMain by getting {
            dependencies {
                //协程
                api(libs.kotlinx.coroutines.swing)
            }
        }

        iosMain.dependencies {
            api(libs.components.resources)
        }
        wasmJsMain.dependencies {
            api(libs.components.resources)
        }
        jsMain.dependencies {
            api(libs.components.resources)
        }
    }
}

android {
    compileSdk = 34
    namespace = "com.lt.compose_views"
    defaultConfig {
        minSdk = 21
        sourceSets["main"].manifest.srcFile("src/main/AndroidManifest.xml")
        sourceSets["main"].res.srcDir("resources")

        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
}



buildscript {
    dependencies {
        val dokkaVersion = libs.versions.dokka.get()
        classpath("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    }
}

//group = "io.github.ltttttttttttt"
////上传到mavenCentral命令: ./gradlew publishAllPublicationsToSonatypeRepository
////mavenCentral后台: https://s01.oss.sonatype.org/#stagingRepositories
//version = "${libs.versions.compose.plugin.get()}.beta1"

group = "com.vickyleu.composeviews"
version = "1.0.0"

tasks.withType<PublishToMavenRepository> {
    val isMac = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
    onlyIf {
        isMac.also {
            if (!isMac) logger.error(
                """
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
            )
        }
    }
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap(DokkaTask::outputDirectory))
    archiveClassifier = "javadoc"
}

tasks.dokkaHtml {
    // outputDirectory = layout.buildDirectory.get().resolve("dokka")
    offlineMode = false
    moduleName = "composeviews"

    // See the buildscript block above and also
    // https://github.com/Kotlin/dokka/issues/2406
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
//        customAssets = listOf(file("../asset/logo-icon.svg"))
//        customStyleSheets = listOf(file("../asset/logo-styles.css"))
        separateInheritedMembers = true
    }

    dokkaSourceSets {
        configureEach {
            reportUndocumented = true
            noAndroidSdkLink = false
            noStdlibLink = false
            noJdkLink = false
            jdkVersion = libs.versions.jvmTarget.get().toInt()
            // sourceLink {
            //     // Unix based directory relative path to the root of the project (where you execute gradle respectively).
            //     // localDirectory.set(file("src/main/kotlin"))
            //     // URL showing where the source code can be accessed through the web browser
            //     // remoteUrl = uri("https://github.com/mahozad/${project.name}/blob/main/${project.name}/src/main/kotlin").toURL()
            //     // Suffix which is used to append the line number to the URL. Use #L for GitHub
            //     remoteLineSuffix = "#L"
            // }
        }
    }
}

val properties = Properties().apply {
    runCatching { rootProject.file("local.properties") }
        .getOrNull()
        .takeIf { it?.exists() ?: false }
        ?.reader()
        ?.use(::load)
}
// For information about signing.* properties,
// see comments on signing { ... } block below
val environment: Map<String, String?> = System.getenv()
extra["githubToken"] = properties["github.token"] as? String
    ?: environment["GITHUB_TOKEN"] ?: ""

publishing {
    val projectName = rootProject.name
    repositories {
        /*maven {
            name = "CustomLocal"
            url = uri("file://${layout.buildDirectory.get()}/local-repository")
        }
        maven {
            name = "MavenCentral"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = extra["ossrhUsername"]?.toString()
                password = extra["ossrhPassword"]?.toString()
            }
        }*/
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/vickyleu/${projectName}")
            credentials {
                username = "vickyleu"
                password = extra["githubToken"]?.toString()
            }
        }
    }
    publications.withType<MavenPublication> {
        artifact(javadocJar) // Required a workaround. See below
        pom {
            url = "https://github.com/vickyleu/${projectName}"
            name = projectName
            description = """
                Visit the project on GitHub to learn more.
            """.trimIndent()
            inceptionYear = "2024"
            licenses {
                license {
                    name = "Apache-2.0 License"
                    url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "kevinnzou"
                    name = "kevinnzou"
                    email = ""
                    roles = listOf("Netease Mobile Developer")
                    timezone = "GMT+8"
                }
            }
            contributors {
                // contributor {}
            }
            scm {
                tag = "HEAD"
                url = "https://github.com/vickyleu/${projectName}"
                connection = "scm:git:github.com/vickyleu/${projectName}.git"
                developerConnection = "scm:git:ssh://github.com/vickyleu/${projectName}.git"
            }
            issueManagement {
                system = "GitHub"
                url = "https://github.com/vickyleu/${projectName}/issues"
            }
            ciManagement {
                system = "GitHub Actions"
                url = "https://github.com/vickyleu/${projectName}/actions"
            }
        }
    }
}

// TODO: Remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
//  Thanks to KSoup repository for this code snippet
tasks.withType(AbstractPublishToMaven::class).configureEach {
    dependsOn(tasks.withType(Sign::class))
}

// * Uses signing.* properties defined in gradle.properties in ~/.gradle/ or project root
// * Can also pass from command line like below
// * ./gradlew task -Psigning.secretKeyRingFile=... -Psigning.password=... -Psigning.keyId=...
// * See https://docs.gradle.org/current/userguide/signing_plugin.html
// * and https://stackoverflow.com/a/67115705
/*signing {
    sign(publishing.publications)
}*/
