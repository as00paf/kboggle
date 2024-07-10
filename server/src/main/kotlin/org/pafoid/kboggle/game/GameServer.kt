package org.pafoid.kboggle.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.pafoid.kboggle.game.state.GameState
import java.util.*
import kotlin.concurrent.schedule

class GameServer(private val config: BoggleConfig) {
    var gameState = GameState.INIT

    private val users = mutableListOf<User>()
    private val prevUsers = mutableListOf<User>()
    private var isGameStarted = false
    private val currentWords = mutableListOf<String>()
    private var currentMaxScore = 0
    private var currentTime = config.gameLength
    private val solver = Solver()
    private var timer = Timer()
    private val userFoundWords = mutableListOf<String>()

    private var board: Board? = null

    data class Data(val users:List<User>, val prevUsers: List<User>, val isGameStarted: Boolean, val currentWords: List<String>, val currentMaxScore: Int, val currentTime:Int)

    val data: Data by lazy { Data(users, prevUsers, isGameStarted, currentWords, currentMaxScore, currentTime) }

    init {
        println("Game server initialized")
    }

    fun initGame() {
        currentTime = config.gameLength

        CoroutineScope(Dispatchers.IO).launch {
            solver.dictionary = solver.loadWordsFromResources(Boggle.DICTIONARY).filter { it.length >= 3 }
            startGame()
        }
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
            endGame()
            return
        }

        currentTime--
    }

    private fun endGame() {
        println("Game ended, starting a new one in ${config.endScreenLength} seconds")

        timer.cancel()

        changeGameState(GameState.ENDED)

        currentTime = config.endScreenLength
        timer = Timer()
        timer.schedule(0, config.interval) {
            waitForRestart()
        }
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

    fun join(user: User):Boolean {
        if(!users.map { it.name }.contains(user.name)) {
            if(!isValidUserName(user.name)) return false
            val newUser = User(UUID.randomUUID().toString(), user.name)
            users.add(newUser)
            println("User: ${newUser.name} joined the game with id ${newUser.id} ")
            return true
        }

        return false
    }
}