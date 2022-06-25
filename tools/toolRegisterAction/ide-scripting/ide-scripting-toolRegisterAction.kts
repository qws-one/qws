
import java.io.File


typealias IdeScriptEngineMgr = IdeScriptEngineManager
typealias ApplicationMgr = ApplicationManager
typealias FileDocumentMgr = FileDocumentManager
typealias ProjectMgr = ProjectManager
typealias IDE_typealias = IDE


@Suppress("FunctionName")
object ToolRegisterActionFromFile {
    private fun sha1sum(str: String): String? = java.security.MessageDigest.getInstance("SHA-1")?.run {
        reset()
        update(str.toByteArray())
        java.lang.String.format("%040x", java.math.BigInteger(1, digest()))
    }

    fun `do for file from project base path`(projectBasePath: String, bindings: Map<String, Any?>, filePath: String) {
        for (engineInfo in IdeScriptEngineMgr.getInstance().engineInfos)
            if (engineInfo.fileExtensions.contains("kts")) IdeScriptEngineMgr.getInstance().getEngine(engineInfo, null as ClassLoader?)
                ?.run {
                    val ide = "IDE".let { bindings[it] as IDE_typealias }
                    val scriptFile = File(filePath)
                    val moduleDir = scriptFile.parentFile.parentFile
                    val libFile /*     */ = File(moduleDir, "lib/Lib.kt")
                    val typealiasFile/**/ = File(moduleDir, "lib/TypeAlias.kt")
                    val importsFile /* */ = File(moduleDir, "tmp/imports.txt")

                    val scriptStr = scriptFile.readText()
                    val scriptSha1 = sha1sum(scriptStr)

                    val script = """
${importsFile.readText()}
${typealiasFile.readText().replace("import typealias4ide.*", "")}
${libFile.readText()}
$scriptStr

val res = ${scriptFile.nameWithoutExtension}.script(arrayOf("${scriptFile.absolutePath}", "$scriptSha1"), bindings)
res
    """
//                    java.io.File(moduleDir, "tmp/register-${scriptFile.name}.kts")
//                        .apply { parentFile.mkdirs(); writeText(script); ide.print("save to :$absolutePath") }
                    try {
                        val res = eval(script)
                        if (null != res && Unit != res) ide.print(res)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ide.error(e)
                    }
                }
    }
}

ProjectMgr.getInstance().openProjects.forEach { project ->
    if ("qws" == project.name) project.basePath?.let { basePath ->
        ApplicationMgr.getApplication().invokeLater {
            ApplicationMgr.getApplication().runWriteAction { FileDocumentMgr.getInstance().saveAllDocuments() }
            File("$basePath/tools/toolRegisterAction/src/").listFiles()?.forEach {
                ToolRegisterActionFromFile.`do for file from project base path`(basePath, bindings, it.absolutePath)
            }
            ToolRegisterActionFromFile.`do for file from project base path`(basePath, bindings, "$basePath/tools/toolRegisterAction/tool/ReloadActions.kt")
        }
    }
}

