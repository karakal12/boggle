package com.amibar.boggle.data

/**
 * Enumeration representing the role of a player in a multiplayer game.
 */
enum class PlayerRole {
    /** The player who created the room and starts the game.  */
    Host,

    /** A player who joined an existing room.  */
    Guest
}
