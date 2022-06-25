plugins {
    kotlin("jvm")
}

sourceSets.main { java.srcDirs("src") }

dependencies {
    implementation(project(":tools:tool4config"))
    implementation(project(":libs:libLocalHostSocket"))
}
