
plugins {
    kotlin("jvm") version "1.7.10"
}
val buildPlace by extra(file(".all.build.place.txt").readLines().first())
allprojects {
    repositories {
        mavenCentral()
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    buildDir = file(buildPlace + path.replace(':', '/')+"/build")
}
