//

object BuildDescInitPlus {

    @JvmStatic
    fun main(args: Array<String>) {
        println(" BuildDescInitPlus.main ${args.toList()}")
        BuildDesc.onAppInit(java.io.File(BuildDescInit.place).absolutePath)
        with(BuildDesc) {
            libTools.mapConfigured.values.forEach { projectsSetPlace ->
                projectsSetPlace.mapOfProjects.values.forEach { subProject ->
                    if (subProject.mainClassToRun.valid) {
                        val dir = file(projectsSetPlace.placeDir, subProject.moduleRelativePath())
                        val processBuilder = ProcessBuilder("$opt_local_gradle/bin/gradle", "${subProject.id}:run")
                            .apply { environment().clear() }
                            .directory(dir)
                        processBuilder.inheritIO()
                        val process = processBuilder.start()
                        BuildDescRunGradle.echo(dir, process?.pid())
                        process.waitFor()
                        //    val process = Runtime.getRuntime().exec(arrayOf("$opt_local_gradle/bin/gradle", "${subProject.id}:run"), emptyArray(), dir)
                        //    java.io.BufferedReader(java.io.InputStreamReader(process.inputStream)).use { input ->
                        //        var line: String?
                        //        while (input.readLine().also { line = it } != null) println(line)
                        //    }
                    }
                }
            }
        }
    }
}