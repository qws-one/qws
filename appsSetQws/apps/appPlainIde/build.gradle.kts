plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:SimpleScript"))
    implementation(project(":tools:Config"))
    implementation(project(":tools:toolPlainIdeListener"))
}
sourceSets.main { java.srcDirs("src_main", "src_module_info") }