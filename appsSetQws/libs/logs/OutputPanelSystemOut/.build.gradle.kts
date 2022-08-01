plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:logs:OutputPanel"))
}
sourceSets.main { java.srcDirs("src_main") }