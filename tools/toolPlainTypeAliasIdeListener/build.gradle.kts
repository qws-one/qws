plugins {
    kotlin("jvm")
}

sourceSets.main { java.srcDirs("src") }

dependencies {
    implementation(project(":tools:tool4config"))
    implementation(project(":tools:tool4TypeAlias4IdeLib"))
    implementation(project(":libs:libLocalHostSocket"))
}
