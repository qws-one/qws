plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:ScriptStr"))
    implementation(project(":libs:Util"))
    implementation(project(":libs:logs:OutputPanel"))
    implementation(project(":tools:BuildDescConst"))
    implementation(project(":tools:ide:Lib"))
    implementation(project(":tools:ide:TypeAlias"))
}
sourceSets.main { java.srcDirs("src_main") }