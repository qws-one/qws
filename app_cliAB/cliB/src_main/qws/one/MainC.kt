package qws.one

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.io.File

object MainC {

    @JvmStatic
    fun main(args: Array<String>) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")
        val dPath = "tmp/sample_m_b5.db"
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dPath")

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

        if (!File(dPath).exists()) {
            File(dPath).parentFile.mkdirs()
            OneDatabase.Schema.create(driver)
            BeanSimple.Word.symbols.forEach { putText(it.text) }
        }

        fun getText(id: Long): String {
            if (id <= 0) return ""
            return database.sqWordQueries.transactionWithResult {
                database.sqWordQueries.selectById(id).executeAsOneOrNull()?.text ?: ""
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
                database.sqListQueries.insertList(resultStr)
                database.sqListQueries.selectByListIds(resultStr).executeAsOneOrNull()?.id ?: -1
            }
        }

        fun getTexts(id: Long): List<String> {
            if (id <= 0) return emptyList()
            return database.sqListQueries.transactionWithResult {
                val listIds = database.sqListQueries.selectById(id).executeAsOneOrNull()?.listIds ?: emptyList()
                listIds.map { getText(it.toLong()) }
            }
        }

        fun putIds(ids: List<String>): Long {
            if (ids.isEmpty()) return 0
            return database.sqListQueries.transactionWithResult {
                database.sqListQueries.insertList(ids)
                database.sqListQueries.selectByListIds(ids).executeAsOneOrNull()?.id ?: -1
            }
        }

        fun getIds(id: Long): List<String> {
            if (id <= 0) return emptyList()
            return database.sqListQueries.transactionWithResult {
                database.sqListQueries.selectById(id).executeAsOneOrNull()?.listIds ?: emptyList()
            }
        }

        fun putLine(line: BeanSimple.Line): Long = line.list.map { it.text }.let { putTexts(it) }
        fun getLine(lineId: Long) = BeanSimple.Line.from(getTexts(lineId))

        fun putParagraph(paragraph: BeanSimple.Paragraph): Long =
            paragraph.lines.map { putLine(it).toString() }.let { putIds(it) }

        fun getParagraph(paragraphId: Long): BeanSimple.Paragraph =
            getIds(paragraphId).map { getLine(it.toLong()) }.let { BeanSimple.Paragraph(it) }

        fun putPoem(poem: BeanSimple.Poem) {
            val paragraphsListId = poem.paragraphs.map { putParagraph(it).toString() }.let { putIds(it) }
            database.sqPoemQueries.transaction {
                database.sqPoemQueries.insertPoem(paragraphsListId, poem.title, poem.author)
            }
        }

        fun getPoem(title: String): BeanSimple.Poem {
            return database.sqPoemQueries.transactionWithResult {
                database.sqPoemQueries.selectByTitle(title).executeAsOneOrNull()
            }?.let { sqPoem ->
                BeanSimple.Poem(
                    sqPoem.author,
                    sqPoem.title,
                    getIds(sqPoem.paragraphsListId).map { getParagraph(it.toLong()) }
                )
            } ?: BeanSimple.Poem.empty
        }

        fun getPoems(author: String): List<BeanSimple.Poem> {
            return database.sqPoemQueries.transactionWithResult {
                database.sqPoemQueries.selectByAuthor(author).executeAsList()
            }.map { sqPoem ->
                BeanSimple.Poem(
                    sqPoem.author,
                    sqPoem.title,
                    getIds(sqPoem.paragraphsListId).map { getParagraph(it.toLong()) }
                )
            }
        }

//        BeanSimple.Poem.examples.forEach { putPoem(it) }
//        putPoem(BeanSimple.Poem.examples[1])

        assert(BeanSimple.Poem.examples[1] == getPoem(BeanSimple.Poem.examples[1].title))

        BeanSimple.Poem.examples.forEach {
            println(getPoem(it.title).text())
        }
        println(BeanSimple.Poem.examples[1])

//        println(database.sqWordQueries.selectAll().executeAsList())

//        val lineA = BeanSimple.Line.from("one", "two")
//        val lineB = BeanSimple.Line.from("table", "one", "two")
//
//
//        val paragraphsA = BeanSimple.Poem.examples[0].paragraphs[0]
//
//        println((paragraphsA))
//        println(getParagraph(26))

//        println(putParagraph(paragraphsA))
//
//        println(database.sqWordQueries.selectAll().executeAsList())
//        println(database.sqListQueries.selectAll().executeAsList())

////        println(putTexts(listOf("one", "two")))
//        println(putTexts(listOf("one", "two")))
//        println(putTexts(listOf("table", "one", "two")))
//        println(putLine(lineA))
//        println(putLine(lineB))
//
////        println(database.sqWordQueries.selectAll().executeAsList())
////
//        println(database.sqListQueries.selectAll().executeAsList())
//        println(getText(1))
//        println(getTexts(1))
//        println(getTexts(2))
//        println(getLine(1))
//        println(getLine(2))

        driver.close()
    }
}