import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties
import java.util.concurrent.Executors

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

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.20")
    }
}
plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("multiplatform").apply(false)
    id("com.android.application").apply(false)
    id("com.android.library").apply(false)
    id("org.jetbrains.compose").apply(false)
    id("com.google.devtools.ksp").apply(false)
    kotlin("plugin.compose").apply(false)
}

group = "com.github.ltttttttttttt"
version = "1.0.2"


tasks{
    register("deletePackages") {
        val rootProjectName = rootDir.name.lowercase()
        val mavenAuthor = "vickyleu"
        //com.vickyleu.composeviews
        val mavenGroup = "com.$mavenAuthor.$rootProjectName"
        group = "publishing"
        description = "Delete all packages in the GitHub Packages registry"
        val keyword = "${mavenGroup}"
        println("keyword: $keyword")
        val properties = Properties().apply {
            runCatching { rootProject.file("local.properties") }
                .getOrNull()
                .takeIf { it?.exists() ?: false }
                ?.reader()
                ?.use(::load)
        }
        val environment: Map<String, String?> = System.getenv()
        val myExtra = mutableMapOf<String, Any>()
        myExtra["githubToken"] = properties["github.token"] as? String
            ?: environment["GITHUB_TOKEN"] ?: ""
        val headers = mapOf(
            "Accept" to "application/vnd.github.v3+json",
            "Authorization" to "Bearer ${myExtra["githubToken"]}",
            "X-GitHub-Api-Version" to "2022-11-28"
        )
        doLast {
            runBlocking {
                val executor = Executors.newFixedThreadPool(10)
                val scope = CoroutineScope(executor.asCoroutineDispatcher())
                val fetchJobs = packageTypes.flatMap { packageType ->
                    visibilityTypes.map { visibility ->
                        scope.async {
                            fetchPackages(packageType, visibility, headers)
                        }
                    }
                }
                fetchJobs.awaitAll().forEach { packages ->
                    allPackages.addAll(packages)
                }
                val deleteJobs = allPackages.filter { pkg ->
                    val packageName = pkg["name"] as String
                    packageName.contains(keyword)
                }.map { pkg ->
                    val packageType = pkg["package_type"] as String
                    val packageName = pkg["name"] as String
                    scope.async {
                        deletePackage(packageType, packageName, headers)
                    }
                }
                try {
                    deleteJobs.awaitAll()
                    executor.shutdown()
                } catch (e: Exception) {
                    println("删除包失败: ${e.message}")
                }
            }
        }
    }
}

val packageTypes = listOf("npm", "maven", "docker", "container")
val visibilityTypes = listOf("public", "private", "internal")
val allPackages = mutableListOf<Map<String, Any>>()

fun fetchPackages(packageType: String, visibility: String, headers: Map<String, String>): List<Map<String, Any>> {
    val packages = mutableListOf<Map<String, Any>>()
    var page = 1

    while (true) {
        val url =
            URL("https://api.github.com/user/packages?package_type=$packageType&visibility=$visibility&page=$page&per_page=100")
        val connection = url.openConnection() as HttpURLConnection

        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val batch: List<Map<String, Any>> = jacksonObjectMapper().readValue(response)
            if (batch.isEmpty()) break
            packages.addAll(batch)
            page++
        } else {
            println("获取$packageType ($visibility) 包列表失败，错误代码: ${connection.responseCode} ${connection.responseMessage}")
            println(connection.inputStream.bufferedReader().use { it.readText() })
            break
        }
    }

    return packages
}

fun deletePackage(packageType: String, packageName: String, headers: Map<String, String>) {
    val url = URL("https://api.github.com/user/packages/$packageType/$packageName")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "DELETE"
    headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

    if (connection.responseCode == 204 || connection.responseCode == 200) {
        println("$packageName 删除成功")
    } else {
        println("$packageName 删除失败，错误代码: ${connection.responseCode}")
        println(connection.inputStream.bufferedReader().use { it.readText() })
    }
}
