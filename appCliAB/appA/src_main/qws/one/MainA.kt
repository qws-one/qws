package qws.one

import kotlinx.dnq.*
import kotlinx.dnq.store.container.StaticStoreContainer

typealias DbEntity = jetbrains.exodus.entitystore.Entity

//https://github.com/JetBrains/xodus-dnq
object MainA {

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
        var author by xdRequiredStringProp()
        val paragraphs by xdLink0_N(Paragraph)
    }

    val poemsStore by lazy {
        XdModel.registerNodes(Word, Paragraph, Poem)
        StaticStoreContainer.init(dbFolder = Conf.dBxd(), entityStoreName = "poemsStore")
//        StaticStoreContainer.init(dbFolder = Conf.dBxd(), entityStoreName = "poemsPlace")
    }

    object Empty {
        val word = poemsStore.transactional { Word.new { text = "empty" } }
        val paragraph = poemsStore.transactional { Paragraph.new { words.add(word) } }
        val poem = poemsStore.transactional { Poem.new { paragraphs.add(paragraph) } }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")
//        println(" "+Empty.word)
//        println(" "+Empty.word.text)
//        poemsStore.transactional(readonly = true) {
//            for (poem in Poem.all().asIterable()) {
//                println("CliA0.main ${poem.title}")
//                for (paragraph in poem.paragraphs) {
//                    for (word in paragraph.words) {
//                        println(" ${word.text}")
//                    }
//                }
//            }
//        }
    }
}
