//
plugins {
    kotlin("jvm")
}
//
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":tools:Config"))
}
//
sourceSets.main { java.srcDirs("src") }