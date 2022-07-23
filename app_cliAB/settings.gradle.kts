rootProject.buildFileName = ".root.gradle.kts"
":appA".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":cliB".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libA".let { include(it); project(it).buildFileName = ".build.gradle.kts" }