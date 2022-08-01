plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:BaseTypeAlias"))
    implementation(project(":libs:logs:LogSimple"))
}
sourceSets.main { java.srcDirs("src_main") }