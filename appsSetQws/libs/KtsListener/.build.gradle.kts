plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:OutputPanel"))
    implementation(project(":libs:Util"))
}
sourceSets.main { java.srcDirs("src_main") }