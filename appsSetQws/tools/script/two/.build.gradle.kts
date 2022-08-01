plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:ScriptStr"))
    implementation(project(":tools:Config"))
    implementation(project(":tools:Module"))
    implementation(project(":tools:RunScriptStr"))
}
sourceSets.main { java.srcDirs("src_main", "src_module_info") }