import com.example.hanjabwv.Hanja
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

suspend fun fetchData(character: String): Hanja {
    val client = OkHttpClient()

    // Задаем URL для запросов
    val hanjaUrl = "https://en.wiktionary.org/wiki/$character"

    // Создаем запросы
    val requestHanja = Request.Builder().url(hanjaUrl).build()

    // Парсим HTML
    val docum = Jsoup.connect(hanjaUrl).get()

    val umhoon = mutableListOf<String>()
    val translationsList = mutableListOf<List<String>>()

    val wordDefinitions = mutableListOf<Pair<String, String>>()

    // Извлечение слов из <p> тегов
    val headwordLines = docum.select("p .headword-line")

    for (line in headwordLines) {
        val koreanWord = line.select("strong.Kore").first()?.text() ?: ""
        val word1 = line.select("b.Kore a").getOrNull(0)?.text() ?: ""
        val word2 = line.select("b.Kore a").getOrNull(1)?.text() ?: ""
        // только для двух, надо для любого числа

        if (koreanWord.isNotEmpty() && word1.isNotEmpty()) {
            wordDefinitions.add(Pair(word1, word2))
        }
    }

    // Используем селектор для поиска только корейских значений
    val glosses = docum.select("li:has(span.form-of-definition i[lang=ko]) span.mention-gloss")

    // Список для хранения результата
    val extractedWords = mutableListOf<String>()

    // Извлекаем текст из каждого элемента и формируем список в формате (word)
    glosses.forEach { gloss ->
        val word = gloss.text() // Извлекаем текст внутри тега <span class="mention-gloss">
        extractedWords.add("($word)")
    }

    val wordPairs = mutableListOf<Pair<String, String>>()

    // Ищем все элементы списка
    val listItems = docum.select("ul li")

    for (item in listItems) {
        val koreanWord = item.select("span.Kore a").first()?.text() ?: ""
        val hanjaWord = item.select("span.Kore a").getOrNull(1)?.text() ?: ""

        if (koreanWord.isNotEmpty() && hanjaWord.isNotEmpty()) {
            wordPairs.add(Pair(koreanWord, hanjaWord))
        }
    }

    return Hanja(wordDefinitions.toString(), extractedWords.toString(), wordPairs.toString())
}
