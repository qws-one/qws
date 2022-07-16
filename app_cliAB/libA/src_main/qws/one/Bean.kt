package qws.one

object Bean {
    sealed interface Str {
        companion object {
            fun looksLikePunctuationMark(str: String, elseBlock: (String) -> Str) =
                if (str.isEmpty()) PunctuationMark.empty else if (PunctuationMark.valid(str)) PunctuationMark(str[0]) else elseBlock(str)
        }
    }

    data class PunctuationMark(val symbol: Char) : Str {
        companion object {
            val symbols = setOf('.', ',', ';', '–')
            val empty = PunctuationMark(' ')
            fun valid(str: String) = str.length == 1 && symbols.contains(str[0])
        }
    }

    data class Word(val text: String) : Str {
        companion object {
            val empty = Word("")
        }
    }

    data class Line(val list: List<Str>) {
        companion object {
            val empty = Line(emptyList())
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
            val examples by lazy { Creation.examples.map { from(it) }.toTypedArray() }

            infix fun from(creation: Creation) = Poem(creation.author, creation.title, creation.text.split("\n\n").map { paragraph ->
                Paragraph(paragraph.lines().map { line ->
                    Line(line.split(' ').map { str -> Str.looksLikePunctuationMark(str.trim()) { word -> Word(word) } })
                })
            })
        }
    }

    data class Creation(val author: String, val title: String, val text: String) {
        companion object {
            infix fun silentFrom(str: String) = _from(str, false)
            infix fun from(str: String) = _from(str, true)

            @Suppress("FunctionName")
            private fun _from(str: String, debug: Boolean = false): Creation {
                fun out(str: String) = if (debug) println(str) else Unit
                out("Creation.from        })>$str<({")
                val (author, title, text) = str.trim().split("\n\n\n").map { it.trim() }
                out("Creation.from author })>$author<({")
                out("Creation.from title  })>$title<({")
                out("Creation.from text   })>$text<({")
                val res = Creation(author, title, text)
                out("Creation.from res    })>$res<({")
                return res
            }

            val examples by lazy {
                arrayOf(
                    """
Тарас Шевченко


Садок вишневий коло хати


Садок вишневий коло хати ,
Хрущі над вишнями гудуть .
Плугатарі з плугами йдуть ,
Співають , ідучи, дівчата ,
А матері вечерять ждуть .

Сім’я вечеря коло хати ,
Вечірня зіронька встає .
Дочка вечерять подає ,
А мати хоче научати ,
Так соловейко не дає .

Поклала мати коло хати
Маленьких діточок своїх ,
Сама заснула коло їх .
Затихло все , тілько дівчата
Та соловейко не затих .
        """.trim(), """
Тарас Шевченко


Встала весна


Встала весна , чорну землю
Сонну розбудила ,
Уквітчала її рястом ,
Барвінком укрила ;
І на полі жайворонок ,
Соловейко в гаї
Землю , убрану весною ,
Вранці зустрічає .
        """.trim(), """
Іван Франко


Надійшла весна

 
Надійшла весна прекрасна,
многоцвітна, тепла, ясна,
наче дівчина в вінку.
Зацвіли луги, діброви,
повно гомону, розмови
і пісень в чагарнику.
        """.trim(), """
Дмитро Павличко


Синиця

 
Синичка дзьобала сало,
Прив´язане до вікна,
А потім сала не стало,
Та все прилітала вона.

На скрипці грала під хатою,
Але не просила їди,
Бо стала сама багатою –
Минулися холоди.

І з вдячності вона грала,
Неначе скрипаль-корифей.
А хата їй відповідала
Щасливим сміхом дітей.
        """.trim(), """
Дмитро Павличко


Весна

 
До мого вікна
Підійшла весна,
Розтопилася на шибці
Квітка льодяна.

Крізь прозоре скло
Сонечко зайшло
І поклало теплу руку
На моє чоло.

Видалось мені,
Що лежу я в сні,
Що співає мені мати
Золоті пісні,

Що мене торка
Ніжна і легка,
Наче те весняне сонце,
Мамина рука.
""".trim(), """
Дмитро Павличко
    
    
Заєць

 
Заєць має двоє вух.
Як одним він рухає,
Другим слухає вітрець,
Що за полем дмухає.

Одним вухом чує спів
Миші під копицею,
Другим чує, як іде
Дядечко з рушницею.

Так пасеться він собі
Врунами зеленими,
Так працює цілий день
Вухами-антенами.

А коли настане ніч,
Спатоньки вкладається –
Одне вухо стеле він,
Другим накривається.
""".trim()
                ).map { _from(it) }.toTypedArray()
            }
        }
    }
}