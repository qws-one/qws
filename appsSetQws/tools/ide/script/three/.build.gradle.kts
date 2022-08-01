plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:ScriptStr"))
    implementation(project(":libs:ScriptStrRunEnv"))
    implementation(project(":libs:SimpleReflect"))
    implementation(project(":libs:Util"))
    implementation(project(":libs:logs:OutputPanel"))
    implementation(project(":tools:Config"))
    implementation(project(":tools:Module"))
    implementation(project(":tools:RunScriptStr"))
    implementation(project(":tools:ide:Lib"))
    implementation(project(":tools:ide:TypeAlias"))
}
sourceSets.main { java.srcDirs("src_main", "src_module_info", "src_one") }