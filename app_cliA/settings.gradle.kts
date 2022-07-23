rootProject.buildFileName = ".root.gradle.kts"
":cliA".let { include(it); project(it).buildFileName = ".build.gradle.kts" }