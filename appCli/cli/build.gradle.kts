plugins {
    kotlin("jvm")
    application
}

sourceSets.main { java.srcDirs("src_main") }

application {
    mainClass.set("Cli")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../appCli/cli/build.gradle.kts run
// sh -c 'cd .../appCli/cli/ && /opt/local/gradle/bin/gradle run'