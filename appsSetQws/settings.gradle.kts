rootProject.buildFileName = ".root.gradle.kts"
":apps:appA".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":apps:appA_run1".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":apps:appA_run2".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":apps:appB".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":apps:appC".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":apps:appPlainIde".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:BaseTypeAlias".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:KtsListener".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:LocalHostSocket".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:ScriptStr".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:ScriptStrRunEnv".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:SimpleReflect".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:Util".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:libQws".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:libQwsEmptyImpl".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:logs:LogSimple".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:logs:OutputPanel".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":libs:logs:OutputPanelSystemOut".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:BuildDescConst".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:Config".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:Module".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:RunScriptStr".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:ide:ActionRegister".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:ide:KtsListener".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:ide:Lib".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:ide:TypeAlias".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:ide:action:ideAction".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:ide:script:three".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:script:four".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:script:one".let { include(it); project(it).buildFileName = ".build.gradle.kts" }
":tools:script:two".let { include(it); project(it).buildFileName = ".build.gradle.kts" }