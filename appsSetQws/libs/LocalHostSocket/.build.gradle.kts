plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:BaseTypeAlias"))
}
sourceSets.main { java.srcDirs("src_main") }