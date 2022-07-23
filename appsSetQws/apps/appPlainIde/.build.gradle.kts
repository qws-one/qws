plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:ScriptStr"))
    implementation(project(":tools:Config"))
}
sourceSets.main { java.srcDirs("src_main") }