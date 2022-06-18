
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":libs:libLocalHost"))
    implementation(project(":libs:libQws"))
    implementation(project(":apps:appA"))
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223")
}