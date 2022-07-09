package qws.one

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.io.File

object MainB {

    @JvmStatic
    fun main(args: Array<String>) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")
        val dPath = "tmp/sample_m_b2.db"
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dPath")
        if (!File(dPath).exists()) {
            File(dPath).parentFile.mkdirs()
            OneDatabase.Schema.create(driver)
        }

        val listOfStringsAdapter = object : ColumnAdapter<List<String>, String> {
            override fun decode(databaseValue: String) =
                if (databaseValue.isEmpty()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }

            override fun encode(value: List<String>) = value.joinToString(",")
        }
        val database = OneDatabase(driver, SqListOfLong.Adapter(listOfStringsAdapter))

        fun putText(text: String): Long {
            if (text.isEmpty()) return 0
            return database.sqWordQueries.transactionWithResult {
                database.sqWordQueries.insertWord(text)
                database.sqWordQueries.selectByText(text).executeAsOneOrNull()?.id ?: -1
            }
        }

        fun putTexts(texts: List<String>): Long {
            if (texts.isEmpty()) return 0
            return database.sqListQueries.transactionWithResult {
                val result = mutableListOf<Long>()
                val resultStr = mutableListOf<String>()
                for (text in texts) {
                    val id = putText(text)
                    if (id <= 0) TODO()
                    result.add(id)
                    resultStr.add(id.toString())
                }
//                val list = listOfStringsAdapter.encode(resultStr)
                database.sqListQueries.insertList(resultStr)
                database.sqListQueries.selectByListIds(resultStr).executeAsOneOrNull()?.id ?: -1
            }
        }

        println(database.sqWordQueries.selectAll().executeAsList())

        println(putTexts(listOf("one", "two")))
        println(putTexts(listOf("table", "one", "two")))
//        putWord("")
        println(database.sqWordQueries.selectAll().executeAsList())
//        putWord("qa")

        println(database.sqListQueries.selectAll().executeAsList())
        driver.close()
    }
}