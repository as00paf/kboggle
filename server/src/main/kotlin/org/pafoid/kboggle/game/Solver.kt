package org.pafoid.kboggle.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import org.pafoid.kboggle.game.data.Position
import org.pafoid.kboggle.game.data.Trie
import org.pafoid.kboggle.game.data.isValid
import java.util.concurrent.ConcurrentHashMap

class Solver {

    lateinit var dictionary: List<String>

    suspend fun loadWordsFromResources(fileName: String): List<String> = withContext(Dispatchers.IO) {
        val classLoader = this::class.java.classLoader
        val inputStream = classLoader.getResourceAsStream(fileName)
            ?: throw IllegalArgumentException("File not found: $fileName")
        println("Loading dictionary")
        inputStream.bufferedReader().readLines()
        //TODO: replace special chars
    }

    suspend fun solve(board: Board): Set<String> {
        val letters = board.letters.chunked(4)
        val foundWords = ConcurrentHashMap.newKeySet<String>()
        val trie = Trie()
        dictionary.forEach { trie.insert(it) }

        val dispatcher = newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors(), "BoggleDispatcher")
        val jobs = mutableListOf<Job>()

        for (row in letters.indices) {
            for (col in letters[0].indices) {
                val job = CoroutineScope(dispatcher).launch {
                    val visited = Array(letters.size) { BooleanArray(letters[0].size) }
                    dfs(letters, trie, visited, Position(row, col), StringBuilder(), foundWords)
                }
                jobs.add(job)
            }
        }

        jobs.forEach { it.join() }

        println("Found acceptable word: ${foundWords.sorted()}")
        return foundWords
    }

    fun indexOfWord(word: String): Int {
        return dictionary.indexOf(word)
    }

    private fun dfs(
        letters: List<List<Char>>,
        trie: Trie,
        visited: Array<BooleanArray>,
        pos: Position,
        currentWord: StringBuilder,
        foundWords: MutableSet<String>
    ) {
        if (!pos.isValid(letters) || visited[pos.row][pos.col]) {
            return
        }

        currentWord.append(letters[pos.row][pos.col])
        visited[pos.row][pos.col] = true

        val word = currentWord.toString()
        if (trie.contains(word)) {
            foundWords.add(word)
        }

        if (trie.startsWith(word)) {
            directions.map { direction ->
                val newPos = Position(pos.row + direction.row, pos.col + direction.col)
                dfs(letters, trie, visited, newPos, currentWord, foundWords)
            }
        }

        visited[pos.row][pos.col] = false
        currentWord.deleteCharAt(currentWord.length - 1)
    }
}

val directions = listOf(
    Position(-1, -1), Position(-1, 0), Position(-1, 1),
    Position(0, -1), Position(0, 1),
    Position(1, -1), Position(1, 0), Position(1, 1)
)

