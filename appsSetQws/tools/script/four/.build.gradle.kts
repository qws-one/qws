plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:ScriptStr"))
    implementation(project(":tools:RunScriptStr"))
}
sourceSets.main { java.srcDirs("src_main") }