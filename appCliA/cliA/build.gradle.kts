plugins {
    kotlin("jvm")
    application
}
dependencies {
    implementation("org.jetbrains.xodus:dnq:2.0.0")
}
sourceSets.main { java.srcDirs("src_main") }

application {
    mainClass.set("CliA")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../appCliA/cliA/build.gradle.kts run
// sh -c 'cd .../appCliA/cliA/ && /opt/local/gradle/bin/gradle run'