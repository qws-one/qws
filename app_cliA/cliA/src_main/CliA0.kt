import kotlinx.dnq.*
import kotlinx.dnq.query.asIterable
import kotlinx.dnq.query.iterator
import kotlinx.dnq.store.container.StaticStoreContainer
import java.io.File

typealias DbEntity = jetbrains.exodus.entitystore.Entity

//https://github.com/JetBrains/xodus-dnq
object CliA0 {

    class Word(dEntity: DbEntity) : XdEntity(dEntity) {
        companion object : XdNaturalEntityType<Word>()

        var text by xdRequiredStringProp()
    }

    class Paragraph(dEntity: DbEntity) : XdEntity(dEntity) {
        companion object : XdNaturalEntityType<Paragraph>()

        val words by xdLink0_N(Word)
    }

    class Poem(dEntity: DbEntity) : XdEntity(dEntity) {
        companion object : XdNaturalEntityType<Poem>()

        var title by xdRequiredStringProp()
        val paragraphs by xdLink0_N(Paragraph)
    }

    val poemsStore by lazy {
        XdModel.registerNodes(Word, Paragraph, Poem)
        val poemsDB = File(System.getProperty("user.home"), ".poemsDB/cliA")
        StaticStoreContainer.init(dbFolder = poemsDB, environmentName = "poemsStore")
//        StaticStoreContainer.init(dbFolder = poemsDB, entityStoreName = "poemsStore")
//        StaticStoreContainer.init(dbFolder = poemsDB, entityStoreName = "poemsPlace")
    }

    object Empty {
        val word = poemsStore.transactional { Word.new { text = "empty" } }
        val paragraph = poemsStore.transactional { Paragraph.new { words.add(word) } }
        val poem = poemsStore.transactional { Poem.new { paragraphs.add(paragraph) } }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")
        poemsStore.transactional(readonly = true) {
            for (poem in Poem.all().asIterable()) {
                println("CliA0.main ${poem.title}")
                for (paragraph in poem.paragraphs) {
                    for (word in paragraph.words) {
                        println(" ${word.text}")
                    }
                }
            }
        }
    }
}
