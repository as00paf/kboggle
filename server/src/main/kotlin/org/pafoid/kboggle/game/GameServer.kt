package org.pafoid.kboggle.game

import data.Data
import data.User
import data.WordGuess
import game.Board
import game.Boggle
import game.BoggleConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.pafoid.kboggle.game.state.GameState
import java.text.Normalizer
import java.util.*
import kotlin.concurrent.schedule

class GameServer(private val config: BoggleConfig, private val sync:suspend ()->Unit) {
    var gameState = GameState.INIT

    val users = mutableListOf<User>()
    private val prevUsers = mutableListOf<User>()
    private var isGameStarted = false
    private val currentWords = mutableListOf<String>()
    private var currentMaxScore = 0
    private var currentTime = config.gameLength
    private val solver = Solver()
    private var timer = Timer()
    private val userFoundWords = mutableListOf<String>()
    private val winners = mutableListOf<User>()

    private var board: Board? = null

    fun data(): Data { return Data(users, prevUsers, isGameStarted, board, currentWords, currentMaxScore, currentTime, gameState.name, winners) }

    init {
        println("Game server initialized")
    }

    fun initGame() {
        currentTime = config.gameLength

        CoroutineScope(Dispatchers.IO).launch {
            solver.dictionary = solver.loadWordsFromResources(Boggle.DICTIONARY).filter { it.length >= 3 }.map { it.removeAccents() }
            startGame()
        }
    }

    fun String.removeAccents(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
    }

    private fun startGame() {
        CoroutineScope(Dispatchers.IO).launch {
            board = Board()
            currentWords.clear()
            currentWords.addAll(solver.solve(board!!))
            currentMaxScore = calculateScore(currentWords)

            println("Board solved, ${currentWords.size} words are possible for a total of $currentMaxScore points")


            timer.cancel()
            println("Starting a new game")
            isGameStarted = true
            changeGameState(GameState.STARTED)

            currentTime = config.gameLength
            timer = Timer()
            timer.schedule(0, config.interval) {
                gameLoop()
            }
            println("Game started")
        }
    }

    private fun gameLoop() {
        print("Time:$currentTime\r")

        if (currentTime == 0) {
            if(isGameStarted) endGame()
            return
        }

        currentTime--
    }

    private fun endGame() {
        isGameStarted = false
        println("Game ended, starting a new one in ${config.endScreenLength} seconds")
        timer.cancel()

        if(winners.size >= 10 ) {
            winners.removeAt(0)
        }

        if(users.isNotEmpty()) {
            val winner = users.maxBy { it.score }.copy()
            winners.add(winner)
        }

        changeGameState(GameState.ENDED)

        currentTime = config.endScreenLength
        timer = Timer()
        timer.schedule(0, config.interval) {
            waitForRestart()
        }

        CoroutineScope(Dispatchers.IO).launch { sync() }
    }

    private fun waitForRestart() {
        print("Wait time:$currentTime\r")
        if (currentTime == 0) {
            timer.cancel()
            restartGame()
        } else {
            currentTime--
        }
    }

    private fun restartGame() {
        currentTime = config.restartScreenLength
        board = null
        userFoundWords.clear()
        isGameStarted = false
        users.forEach { it.reset() }
        prevUsers.clear()
        prevUsers.addAll(users)
        changeGameState(GameState.RESTARTING)
        timer = Timer()
        timer.schedule(config.restartScreenLength * config.interval) {
            startGame()
        }
    }

    private fun changeGameState(newState: GameState) {
        gameState = newState
        CoroutineScope(Dispatchers.IO).launch { sync() }
    }

    fun calculateScore(words: List<String>): Int {
        val onePoint = words.filter { it.length <= 4 }.size
        val twoPoints = words.filter { it.length == 5 }.size * 2
        val threePoints = words.filter { it.length == 6 }.size * 3
        val fivePoints = words.filter { it.length == 7 }.size * 5
        val elevenPoints = words.filter { it.length >= 8 }.size * 11

        return onePoint + twoPoints + threePoints + fivePoints + elevenPoints
    }

    private fun isValidUserName(name: String): Boolean {
        return true
    }

    fun join(user: User): User? {
        if(users.map { it.name }.contains(user.name)) return users.find { it.name == user.name }
        if(!isValidUserName(user.name)) return null

        val newUser = User(user.id, user.name, 0)
        users.add(newUser)
        println("User: ${newUser.name} joined the game with id ${newUser.id} ")

        return newUser
    }

    fun leave(userId: String) {
        users.find { it.id == userId }?.let{
            println("User ${it.name} left the game")
            users.remove(it)
        }
    }

    fun guessWord(data: WordGuess): Int? {
        val currentFoundWords = users.find { it.id == data.userId }?.foundWords.orEmpty()
        if(!currentWords.contains(data.word) || currentFoundWords.contains(data.word)) return null
        val points = calculateScore(listOf(data.word))
        users.find { it.id == data.userId }?.let { user ->
            user.score += points
            if(points > 0) user.foundWords.add(data.word)
        }

        println("User[${data.userId}] successfully guessed the word : ${data.word} and scored $points points")

        return points
    }
}