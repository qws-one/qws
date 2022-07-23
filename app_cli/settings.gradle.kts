rootProject.buildFileName = ".root.gradle.kts"
":cli".let { include(it); project(it).buildFileName = ".build.gradle.kts" }