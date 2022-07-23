plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:OutputPanel"))
}
sourceSets.main { java.srcDirs("src_main") }