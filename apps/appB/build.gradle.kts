plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:libQws"))
    implementation(project(":libs:libQwsEmptyImpl"))
    implementation(project(":libs:libLocalHostSocket"))
}
