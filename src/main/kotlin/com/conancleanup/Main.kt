package com.conancleanup

import java.sql.DriverManager

fun main() {
    val url = "jdbc:sqlite:game.db"
    val con = DriverManager.getConnection(url)
    con.use { c ->
        c.createStatement().use { s ->
            s.executeQuery("select playerId, char_name from characters").use { rs ->
                while (rs.next()) {
                    val playerId = rs.getLong(1)
                    val name = rs.getString(2)
                    println("Player '$name' with id $playerId")
                }
            }
        }
    }
}
