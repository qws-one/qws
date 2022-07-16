plugins {
    kotlin("jvm")
    application
}
dependencies {
    implementation("org.jetbrains.xodus:dnq:1.4.480")
}
sourceSets.main { java.srcDirs("src_main", "src_module_info") }

application {
    mainClass.set("CliA")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../app_cliA/cliA/build.gradle.kts run
// sh -c 'cd .../app_cliA/cliA/ && /opt/local/gradle/bin/gradle run'