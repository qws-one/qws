plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:SimpleReflect"))
    implementation(project(":libs:SimpleScript"))
    implementation(project(":libs:Util"))
    implementation(project(":tools:Config"))
    implementation(project(":tools:Module"))
    implementation(project(":tools:RunSimpleScript"))
}
sourceSets.main { java.srcDirs("src_main", "src_module_info", "src_one") }