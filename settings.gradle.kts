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
rootProject.buildFileName = "build.gradle.kts"
rootProject.name = "ComposeViews"

include(":views")
include(":common_app")
include(":android_app")
include("desktop_app")
include("js_app")
include("wasm_app")


pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            mavenCentral{
                content{
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.android.tools.*")
                    excludeGroupByRegex("androidx.compose.*")
                    excludeGroupByRegex("com.github.(?!johnrengelman|oshi).*")
                }
            }
            gradlePluginPortal {
                content{
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("androidx.databinding.*")
                    // 避免无效请求,加快gradle 同步依赖的速度
                    excludeGroupByRegex("com.github.(?!johnrengelman).*")
                }
            }
            google {
                content {
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("org.jogamp.*")
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            maven(url = "https://androidx.dev/storage/compose-compiler/repository") {
                content {
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev") {
                content {
                    excludeGroupByRegex("org.jogamp.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            maven {
                setUrl("https://jogamp.org/deployment/maven")
                content {
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    includeGroupByRegex("org.jogamp.*")
                    includeGroupByRegex("dev.datlag.*")
                }
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        google {
            content {
                excludeGroupByRegex("media.kamel.*")
                excludeGroupByRegex("org.jogamp.*")
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech).*")
            }
        }

        // workaround for https://youtrack.jetbrains.com/issue/KT-51379
        maven { setUrl("https://repo.maven.apache.org/maven2")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech|mzule|pwittchen|filippudak|asyl|florent37).*")
            }
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
                excludeGroupByRegex("org.jogamp.*")
                includeModule("org.nodejs", "node")
            }
            isAllowInsecureProtocol = false
        }
        maven {
            setUrl("https://jitpack.io")
            content {
                excludeGroupByRegex("org.jogamp.*")
                includeGroupByRegex("com.github.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
            }
        }
        // 保利威阿里云效
        maven {
            credentials {
                username = "609cc5623a10edbf36da9615"
                password = "EbkbzTNHRJ=P"
            }
            setUrl("https://packages.aliyun.com/maven/repository/2102846-release-8EVsoM/")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("io.github.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech|mzule|pwittchen|filippudak|asyl|florent37).*")
            }
        }
        maven {
            setUrl("https://repo1.maven.org/maven2/")
            content {
                excludeGroupByRegex("org.jetbrains.compose.*")
                includeGroupByRegex("org.jogamp.gluegen.*")
            }
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/public/")
            content {
                excludeGroupByRegex("org.jogamp.*")
                includeGroupByRegex("com.aliyun.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.github.vickyleu.*")
                excludeGroupByRegex("io.github.vickyleu.*")
            }
        }
        maven {
            setUrl("https://maven.aliyun.com/nexus/content/repositories/jcenter/")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech|mzule|pwittchen|filippudak|asyl|florent37).*")
            }
        }
        maven { setUrl("https://maven.pkg.jetbrains.space/public/p/compose/dev")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap")
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
        maven {
            setUrl("https://jogamp.org/deployment/maven")
            content {
                includeGroupByRegex("dev.datlag.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
            }
        }
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental"){
            content {
                excludeGroupByRegex("org.jogamp.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }

    }
}
