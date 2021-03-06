plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:Util"))
    implementation(project(":libs:logs:OutputPanel"))
}
sourceSets.main { java.srcDirs("src_main") }