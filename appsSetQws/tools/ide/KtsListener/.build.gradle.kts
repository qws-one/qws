plugins {
    kotlin("jvm")
    application
}
dependencies {
    implementation(project(":libs:KtsListener"))
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:OutputPanel"))
    implementation(project(":libs:ScriptStr"))
    implementation(project(":libs:ScriptStrRunEnv"))
    implementation(project(":libs:SimpleReflect"))
    implementation(project(":libs:Util"))
    implementation(project(":tools:BuildDescConst"))
    implementation(project(":tools:Config"))
    implementation(project(":tools:Module"))
    implementation(project(":tools:RunScriptStr"))
    implementation(project(":tools:ide:Lib"))
    implementation(project(":tools:ide:TypeAlias"))
}
sourceSets.main { java.srcDirs("src_main", "src_module_info") }

application {
    mainClass.set("KtsListener")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../appsSetQws/tools/ide/KtsListener/.build.gradle.kts run
// sh -c 'cd .../appsSetQws/tools/ide/KtsListener/ && /opt/local/gradle/bin/gradle run'