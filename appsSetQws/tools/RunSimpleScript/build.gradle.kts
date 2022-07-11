plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:Util"))
    implementation(project(":tools:Config"))
    implementation(project(":tools:Module"))
}
sourceSets.main { java.srcDirs("src_main", "src_module_info") }