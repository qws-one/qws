plugins {
    kotlin("jvm")
    application
}
dependencies {
    implementation(project(":libA"))
    implementation("org.jetbrains.xodus:xodus-openAPI:2.0.1")
    implementation("org.jetbrains.xodus:xodus-entity-store:2.0.1")
    implementation("org.jetbrains.xodus:xodus-query:2.0.1")
    implementation("org.jetbrains.xodus:dnq:2.0.0")
}
sourceSets.main { java.srcDirs("src_main") }

application {
    mainClass.set("AppA")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../appCliAB/appA/build.gradle.kts run
// sh -c 'cd .../appCliAB/appA/ && /opt/local/gradle/bin/gradle run'