plugins {
    kotlin("jvm")
    application
}
dependencies {
    implementation(project(":libA"))
    implementation("org.jetbrains.xodus:dnq:1.4.480")
}
sourceSets.main { java.srcDirs("src_main", "src_module_info") }

application {
    mainClass.set("AppA")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../app_cliAB/appA/build.gradle.kts run
// sh -c 'cd .../app_cliAB/appA/ && /opt/local/gradle/bin/gradle run'