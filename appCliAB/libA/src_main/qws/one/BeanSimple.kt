package qws.one

object BeanSimple {

    data class Word(val text: String) {
        companion object {
            val empty = Word("")
            val symbols = Bean.PunctuationMark.symbols.map { Word(it.toString()) }.toSet()
        }
    }

    data class Line(val list: List<Word>) {
        companion object {
            val empty = Line(emptyList())
            fun from(vararg str: String) = Line(str.map { Word(it) })
            fun from(list: List<String>) = Line(list.map { Word(it) })
        }
    }

    data class Paragraph(val lines: List<Line>) {
        companion object {
            val empty = Paragraph(emptyList())
        }
    }

    data class Poem(val author: String, val title: String, val paragraphs: List<Paragraph>) {
        companion object {
            val empty = Poem("", "", emptyList())
            val examples by lazy { Bean.Creation.examples.map { from(it) }.toTypedArray() }

            infix fun from(creation: Bean.Creation) = Poem(creation.author, creation.title, creation.text.split("\n\n")
                .map { paragraph ->
                    Paragraph(paragraph.lines()
                        .map { line ->
                            Line(line.split(' ')
                                .map { str -> Word(str.trim()) })
                        })
                })
        }

        fun text() = paragraphs.joinToString("\n\n") { paragraph ->
            paragraph.lines.joinToString("\n") { it.list.joinToString(" ") { it.text } }
        }
    }
}
