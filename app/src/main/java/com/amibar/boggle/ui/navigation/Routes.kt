package com.amibar.boggle.ui.navigation

import com.amibar.boggle.data.PlayerRole
import kotlinx.serialization.Serializable

@Serializable
object MainMenu

@Serializable
object Singleplayer

@Serializable
data class Multiplayer(val roomCode: String, val playerRole: PlayerRole)

@Serializable
object FriendList
