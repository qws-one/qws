plugins {
    kotlin("jvm")
}
dependencies {
    implementation(project(":apps:appA"))
    implementation(project(":libs:KtsListener"))
    implementation(project(":libs:LocalHostSocket"))
    implementation(project(":libs:ScriptStr"))
    implementation(project(":libs:ScriptStrRunEnv"))
    implementation(project(":libs:SimpleReflect"))
    implementation(project(":libs:Util"))
    implementation(project(":libs:libQws"))
    implementation(project(":libs:logs:OutputPanel"))
    implementation(project(":libs:logs:OutputPanelSystemOut"))
    implementation(project(":tools:BuildDescConst"))
    implementation(project(":tools:Config"))
    implementation(project(":tools:RunScriptStr"))
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223")
}