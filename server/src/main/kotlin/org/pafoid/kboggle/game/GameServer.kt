package org.pafoid.kboggle.game

import Constants.SERVER_PORT
import data.Data
import data.Sync
import data.User
import data.WordGuess
import game.Board
import game.Boggle
import game.BoggleConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.pafoid.kboggle.extensions.removeAccents
import org.pafoid.kboggle.game.state.GameState
import org.pafoid.kboggle.web.SocketService
import java.util.*
import kotlin.concurrent.schedule

class GameServer(private val config: BoggleConfig, private val socketService:SocketService) {
    var gameState = GameState.INIT

    val users = mutableListOf<User>()
    private val prevUsers = mutableListOf<User>()
    private var isGameStarted = false
    private val currentWords = mutableListOf<String>()
    private var currentMaxScore = 0
    private var currentTime = config.gameLength
    private val userFoundWords = mutableListOf<String>()
    private val winners = mutableListOf<User>()

    private val solver = Solver()
    private var timer = Timer()

    private var board: Board? = null

    private var gameJob: Job = Job()

    fun data(): Data { return Data(users, prevUsers, isGameStarted, board, currentWords, currentMaxScore, currentTime, gameState.name, winners) }

    init {
        val port = System.getenv("BOGGLE_SERVER_PORT")?.toInt() ?: SERVER_PORT
        println("KBoggle Game server running on: http://localhost:$port")
    }

    fun initGame() {
        currentTime = config.gameLength

        CoroutineScope(Dispatchers.IO).launch {
            solver.dictionary = solver.loadWordsFromResources(Boggle.DICTIONARY)
                .filter { it.length >= 3 }
                .map { it.removeAccents() }
                .toSet()
            startGame()
        }

        println("Game initialized")
    }

    private fun startGame() {
        gameJob.cancel()
        gameJob = CoroutineScope(Dispatchers.IO).launch {
            board = Board()
            currentWords.clear()
            currentWords.addAll(solver.solve(board!!))
            currentMaxScore = calculateScore(currentWords)

            println("Board solved, ${currentWords.size} words are possible for a total of $currentMaxScore points")
            println("Starting a new game")

            timer.cancel()
            isGameStarted = true
            currentTime = config.gameLength
            timer = Timer()
            timer.schedule(0, config.interval) {
                gameLoop()
            }

            changeGameState(GameState.STARTED)
            println("Game started")
        }

    }

    private fun gameLoop() {
        if (currentTime == 0) {
            if(isGameStarted) endGame()
            return
        }

        currentTime--
        //print("Time:${currentTime.seconds}\r")
    }

    private fun endGame() {
        changeGameState(GameState.ENDED)

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


        currentTime = config.endScreenLength

        timer = Timer()
        timer.schedule(0, config.interval) {
            waitForRestart()
        }

        changeGameState(GameState.RESTARTING) // TODO: test moving this to the schedule call
    }

    private fun waitForRestart() {
        //print("Wait time:$currentTime\r")
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

        System.gc()
    }

    private fun changeGameState(newState: GameState) {
        gameState = newState
        socketService.sendToAll(Sync(data()))
    }

    private fun calculateScore(words: List<String>): Int {
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
            println("${user.name} successfully guessed the word : ${data.word} and scored $points points")
        }

        return points
    }
}