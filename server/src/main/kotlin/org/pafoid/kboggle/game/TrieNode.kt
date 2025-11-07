package org.pafoid.kboggle.game

class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var isEndOfWord = false
}