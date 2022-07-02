//
plugins {
    kotlin("jvm")
}
//
dependencies {
    implementation(project(":libs:libQws"))
}
//
sourceSets.main { java.srcDirs("src") }