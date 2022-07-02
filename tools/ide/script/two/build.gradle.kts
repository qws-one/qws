//
plugins {
    kotlin("jvm")
}
//
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:SimpleReflect"))
    implementation(project(":libs:SimpleScript"))
    implementation(project(":libs:Util"))
    implementation(project(":tools:Config"))
    implementation(project(":tools:Module"))
    implementation(project(":tools:RunSimpleScript"))
    implementation(project(":tools:ide:Lib"))
    implementation(project(":tools:ide:TypeAlias"))
}
//
sourceSets.main { java.srcDirs("src", "main") }