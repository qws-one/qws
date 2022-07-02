//
plugins {
    kotlin("jvm")
}
//
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:libQws"))
    implementation(project(":libs:libQwsEmptyImpl"))
}