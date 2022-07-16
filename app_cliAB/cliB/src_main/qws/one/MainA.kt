package qws.one

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.io.File

object MainA {

    @JvmStatic
    fun main(args: Array<String>) {
        val listOfStringsAdapter = object : ColumnAdapter<List<String>, String> {
            override fun decode(databaseValue: String) =
                if (databaseValue.isEmpty()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }

            override fun encode(value: List<String>) = value.joinToString(",")
        }
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")

//        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val dPath = "tmp/sample22.db"
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dPath")
        if (!File(dPath).exists()) {
            OneDatabase.Schema.create(driver)
        }
        val database = OneDatabase(driver, SqListOfLong.Adapter(listOfStringsAdapter))


        database.sqWordQueries.transaction {
            val str = "one"
            database.sqWordQueries.insertWord(str)
            val aa = database.sqWordQueries.selectByText(str).executeAsOneOrNull()
            val ab = database.sqWordQueries.selectWithMaxId().executeAsOneOrNull()
            println("CliB.main })>$aa $ab<({")
        }

//        val players = database.sqWordQueries.transactionWithResult {
//            database.wordItemQueries.selectAll().executeAsList()
//        }
//
//        val wordItemQueries = database.wordItemQueries
        val sqWordQueries = database.sqWordQueries
//        println(wordItemQueries.selectAll().executeAsOneOrNull())
        println(sqWordQueries.selectAll().executeAsList())
//        println(wordItemQueries.selectById(2).executeAsList())
        println("2  " + sqWordQueries.selectById(2).executeAsList())
        println("10 " + sqWordQueries.selectById(10).executeAsList())
        println(sqWordQueries.selectByText("Round").executeAsList())
        println(sqWordQueries.selectByText("cherry").executeAsList())

        println("MaxId " + sqWordQueries.selectWithMaxId().executeAsOneOrNull())
        sqWordQueries.insertWord("cherry")
        println("MaxId " + sqWordQueries.selectWithMaxId().executeAsOneOrNull())
        sqWordQueries.insertWord("Cherry")
        println("MaxId " + sqWordQueries.selectWithMaxId().executeAsOneOrNull())
        sqWordQueries.insertWord("cherry")
        println("MaxId " + sqWordQueries.selectWithMaxId().executeAsOneOrNull())
        sqWordQueries.insertWord("Round")
        sqWordQueries.insertWord("Table")
        sqWordQueries.insertWord("table")
        println(sqWordQueries.selectAll().executeAsList())
    }
}