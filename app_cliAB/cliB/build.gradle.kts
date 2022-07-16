@file:Suppress("SpellCheckingInspection")

plugins {
    kotlin("jvm")
    id("com.squareup.sqldelight") version "1.5.3"
    application
}
dependencies {
    implementation(project(":libA"))
    implementation("com.squareup.sqldelight:coroutines-extensions-jvm:1.5.3")
    implementation("com.squareup.sqldelight:sqlite-driver:1.5.3")
}
sourceSets.main { java.srcDirs("src_main", "src_module_info") }
sqldelight {
    database("OneDatabase") {
        packageName = "qws.one"
        sourceFolders = listOf("sql")
    }
}
application {
    mainClass.set("CliB")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../app_cliAB/cliB/build.gradle.kts run
// sh -c 'cd .../app_cliAB/cliB/ && /opt/local/gradle/bin/gradle run'