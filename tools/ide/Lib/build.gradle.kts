//
plugins {
    kotlin("jvm")
}
//
dependencies {
    implementation(project(":tools:ide:TypeAlias"))
}
//
sourceSets.main { java.srcDirs("src") }