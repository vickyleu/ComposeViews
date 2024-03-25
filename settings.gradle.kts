@file:Suppress("UnstableApiUsage")

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
import java.util.Properties


rootProject.name = "ComposeViews"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":compviews")


include(":commonApp")
include(":sample")

include(":KMMDataStructure")
project(":KMMDataStructure").projectDir = file("/Volumes/Extra/Github/KMMDataStructure")

include(":KMMDataStructure:datastructure")

//project(":ComposeViews").apply { // versionCatalogs 中无法使用与父项目同名的模块名,会生成子项目的名称(projects.ComposeViews),这样会造成冲突
//    this.name = "CompViews"  //重命名还是有点问题, implementation(projects.ComposeViews)会无法正确加载依赖项目
//}

pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            mavenCentral()
            gradlePluginPortal()
            google {
                content {
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                }
            }
            maven(url = "https://androidx.dev/storage/compose-compiler/repository")
            maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
            maven(url ="https://mirrors.tencent.com/nexus/repository/maven-public/")
            val properties = java.util.Properties().apply {
                runCatching { rootProject.projectDir.resolve("local.properties") }
                    .getOrNull()
                    .takeIf { it?.exists() ?: false }
                    ?.reader()
                    ?.use(::load)
            }
            val environment: Map<String, String?> = System.getenv()
            extra["githubToken"] = properties["github.token"] as? String
                ?: environment["GITHUB_TOKEN"] ?: ""
            maven {
                url = uri("https://maven.pkg.github.com/vickyleu/${rootProject.name}")
                credentials {
                    username = "vickyleu"
                    password = extra["githubToken"]?.toString()
                }
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        maven {
            setUrl("https://jitpack.io")
            content {
                includeGroupByRegex("com.github.*")
            }
        }
        google {
            content {
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        // workaround for https://youtrack.jetbrains.com/issue/KT-51379
        exclusiveContent {
            forRepository {
                ivy("https://download-cdn.jetbrains.com/kotlin/native/builds") {
                    name = "Kotlin Native"
                    patternLayout {
                        // example download URLs:
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/linux-x86_64/kotlin-native-prebuilt-linux-x86_64-1.7.20.tar.gz
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/windows-x86_64/kotlin-native-prebuilt-windows-x86_64-1.7.20.zip
                        // https://download.jetbrains.com/kotlin/native/builds/releases/1.7.20/macos-x86_64/kotlin-native-prebuilt-macos-x86_64-1.7.20.tar.gz
                        listOf(
                            "macos-x86_64",
                            "macos-aarch64",
                            "osx-x86_64",
                            "osx-aarch64",
                            "linux-aarch64",
                            "linux-x86_64",
                            "windows-x86_64",
                        ).forEach { os ->
                            listOf("dev", "releases").forEach { stage ->
                                artifact("$stage/[revision]/$os/[artifact]-$os-[revision].[ext]")
                            }
                        }
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeModuleByRegex(".*", ".*kotlin-native-prebuilt.*") }
        }
        ivy {
            name = "Node.js"
            setUrl("https://nodejs.org/dist")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
            isAllowInsecureProtocol = false
        }
    }
}
