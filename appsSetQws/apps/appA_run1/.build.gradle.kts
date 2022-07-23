plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":apps:appA"))
    implementation(project(":libs:libQws"))
    implementation(project(":tools:Module"))
}
sourceSets.main { java.srcDirs("src_main", "src_module_info", "src_one") }