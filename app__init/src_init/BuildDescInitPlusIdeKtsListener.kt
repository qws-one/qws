object BuildDescInitPlusIdeKtsListener {

    @JvmStatic
    fun main(args: Array<String>) {
        println(" BuildDescInitPlusIdeKtsListener.main ${args.toList()}")
        BuildDesc.onAppInit(java.io.File(BuildDescInit.place).absolutePath)
        with(BuildDescBase) {
            BuildDescBase.lib.refOfObjLib.mapConfigured.values.forEach { projectsSetPlace ->
                projectsSetPlace.mapOfProjects.values.forEach { subProject ->
                    if (subProject.mainUnit.validToRun && subProject == BuildDesc.root.tools.ide.KtsListener) {
                        val dir = file(projectsSetPlace.placeDir, subProject.moduleRelativePath())
                        val processBuilder = ProcessBuilder("$opt_local_gradle/bin/gradle", "${subProject.id}:run")
                            .apply { environment().clear() }
                            .directory(dir)
                        processBuilder.inheritIO()
                        val process = processBuilder.start()
                        BuildDescRunGradle.echo(dir, process?.pid())
                        process.waitFor()
                    }
                }
            }
        }
    }
}