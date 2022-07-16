package qws.one

import java.io.File

object Conf {
    const val dBPlace = ".poemsDB/one/"
    const val dBsq = "$dBPlace/sq"
    const val dBxd = "$dBPlace/xd"

    fun onePlace(path: String) = File(System.getProperty("user.home"), path)
    fun dBsq() = onePlace(dBsq)
    fun dBxd() = onePlace(dBxd)
}