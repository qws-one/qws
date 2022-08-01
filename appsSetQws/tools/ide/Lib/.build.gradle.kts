plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:logs:OutputPanel"))
    implementation(project(":tools:ide:TypeAlias"))
}
sourceSets.main { java.srcDirs("src_main") }