plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":apps:appA"))
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:SimpleReflect"))
    implementation(project(":libs:libQws"))
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223")
}