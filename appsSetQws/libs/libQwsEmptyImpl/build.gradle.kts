plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:libQws"))
}
sourceSets.main { java.srcDirs("src_main", "src_module_info") }