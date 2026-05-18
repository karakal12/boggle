package com.amibar.boggle.ui.shared

import com.amibar.boggle.data.User

/**
 * Shared sample data for use in Compose Previews.
 */
object SampleData {
    val providedWords = listOf(
        "rout", "rut", "roc", "ore", "hit", "hire", "bier", "fet", "eth", "wet",
        "met", "tret", "tref", "tier", "hat", "few", "bite", "coater", "corbie",
        "aortic", "rite", "bertha", "turbeth", "retia", "feria", "coir", "court",
        "merit", "triac", "weir", "mitier", "ourie", "airtime", "ourebi", "refit",
        "timer", "beth", "orbit", "outwrit", "turbith", "cahier", "beta", "hater",
        "mitre", "wert", "fiber", "ate", "metro", "erica", "remit", "thio"
    )

    val solutions = providedWords.associateWith { "" }

    val player1 = User(uid = "1", displayName = "Player 1")
    val player2 = User(uid = "2", displayName = "Player 2")

    val player1Words = listOf("rout", "rut", "hit", "hire", "met", "tier")
    val player2Words = listOf("hit", "hire", "turbeth", "few", "bite", "beta")

    val playersWordsMap = mapOf(
        player1 to player1Words,
        player2 to player2Words
    )

    val board = charArrayOf(
        't', 'h', 'i', 's',
        'i', 's', 'a', 'n',
        'e', 'x', 'a', 'm',
        'p', 'l', 'e', 'Q'
    )
}
