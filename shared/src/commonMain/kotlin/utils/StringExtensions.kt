package utils

import kotlin.random.Random

fun String.selectRandomLetter(): Char {
    if(this.isEmpty()) throw IllegalArgumentException("The word cannot be empty")
    val randomIndex = Random.nextInt(length)
    return this[randomIndex]
}