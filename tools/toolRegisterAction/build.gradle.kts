plugins {
    kotlin("jvm")
}

sourceSets.main { java.srcDirs("src", "lib", "tool") }

dependencies {
    implementation(project(":tools:tool4TypeAlias4IdeLib"))
}
