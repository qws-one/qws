plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":tools:toolPlainIdeListener"))
    implementation(project(":tools:tool4config"))

    implementation(project(":libs:libLocalHostSocket"))
    implementation(project(":libs:libSimpleScript"))
}
