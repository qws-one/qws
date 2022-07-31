//

interface BuildDescConstPlus {
    companion object : BuildDescConstPlus {
        override val app__init = BuildDescConst.app__init
        override val conf_place by lazy { Companion }

        const val of_ide_scripting = "conf.place.of.ide-scripting.txt"
        const val of__tmp_dir_big = "conf.place.of.tmp_dir_big.txt"
        const val of__tmp_dir_quick = "conf.place.of.tmp_dir_quick.txt"
    }

    val app__init: String
    val conf_place: Companion
}