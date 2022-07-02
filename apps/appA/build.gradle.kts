//
plugins {
    kotlin("jvm")
}
//
dependencies {
    implementation(project(":libs:libQws"))
    implementation(project(":libs:libQwsEmptyImpl"))
}
//
sourceSets.main { java.srcDirs("src") }