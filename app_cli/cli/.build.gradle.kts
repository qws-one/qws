plugins {
    kotlin("jvm")
    application
}

sourceSets.main { java.srcDirs("src_main") }

application {
    mainClass.set("Cli")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../app_cli/cli/.build.gradle.kts run
// sh -c 'cd .../app_cli/cli/ && /opt/local/gradle/bin/gradle run'