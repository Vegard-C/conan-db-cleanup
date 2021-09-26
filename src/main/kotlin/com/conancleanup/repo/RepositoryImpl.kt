package com.conancleanup.repo

import com.conancleanup.DbConnectionBuilder
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.ResultSet

@Repository
class RepositoryImpl(private val conBuilder: DbConnectionBuilder) : ConanRepository {
    override fun readAccounts(): Collection<AccountEO> {
        conBuilder.connection().use { c ->
            return c.readTable("select id, user from account") { AccountEO(it.getLong(1), it.getString(2)) }
        }
    }

    override fun readGuilds(): Collection<GuildEO> {
        conBuilder.connection().use { c ->
            return c.readTable("select guildId, name from guilds") { GuildEO(it.getLong(1), it.getString(2)) }
        }
    }

    override fun readPlayers(): Collection<PlayerEO> {
        conBuilder.connection().use { c ->
            return c.readTable("select playerId, id, char_name, lastTimeOnline, guild from characters") { rs ->
                val guild: Any? = rs.getObject(5)
                PlayerEO(
                    id = rs.getLong(1),
                    ownerId = rs.getLong(2),
                    name = rs.getString(3),
                    lastOnlineEpocheSeconds = rs.getLong(4),
                    guildId = if (guild == null) null else (guild as Int).toLong(),
                )
            }
        }
    }

    override fun readBuildingsAndPlaceables(): Collection<BuildingOrPlaceableEO> {
        conBuilder.connection().use { c ->
            return c.readTable("select object_id, owner_id from buildings") {
                BuildingOrPlaceableEO(
                    it.getLong(1),
                    it.getLong(2)
                )
            }
        }
    }

    override fun readBuildingInstances(): Collection<BuildingInstancesEO> {
        val buildingInstanceCount: MutableMap<Long, Long> = mutableMapOf()
        conBuilder.connection().use { c ->
            c.readTable("select object_id from building_instances") {
                val id = it.getLong(1)
                buildingInstanceCount.put(id, buildingInstanceCount.getOrDefault(id, 0) + 1)
            }
        }
        return buildingInstanceCount.map { (key, value) ->  BuildingInstancesEO(key, value)}
    }

    override fun updateOwnership(buildingIds: List<Long>, owner: Long) {
        if (buildingIds.isNotEmpty()) {
            val idSet = buildingIds.joinToString()
            conBuilder.connection().use { c ->
                c.autoCommit = false
                c.createStatement().use { s ->
                    val rc = s.executeUpdate("update buildings set owner_id=$owner where object_id in ($idSet)")
                    println("Execute $rc")
                }
                c.commit()
            }
        }
    }

    override fun deletePlayer(playerId: Long, ownerId: Long) {
        conBuilder.connection().use { c ->
            c.autoCommit = false
            val ownedBuildingIds = mutableListOf<Long>()
            c.readTable("select object_id from buildings where owner_id=$ownerId") {
                ownedBuildingIds.add(it.getLong(1))
            }
            val buildingIdSet = ownedBuildingIds.joinToString()
            if (ownedBuildingIds.isNotEmpty()) {
                c.createStatement().use { s ->
                    val rc = s.executeUpdate("delete from building_instances where object_id in ($buildingIdSet)")
                    println("Deleted building_instances $rc")
                }
                c.createStatement().use { s ->
                    val rc = s.executeUpdate("delete from properties where object_id in ($buildingIdSet)")
                    println("Deleted properties for buildings $rc")
                }
                c.createStatement().use { s ->
                    val rc = s.executeUpdate("delete from actor_position where id in ($buildingIdSet)")
                    println("Deleted actor_position for buildings $rc")
                }
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("delete from properties where object_id=$ownerId")
                println("Deleted properties for char $rc")
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("delete from buildings where owner_id=$ownerId")
                println("Deleted buildings $rc")
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("delete from item_properties where owner_id=$ownerId")
                println("Deleted item_properties of char $rc")
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("delete from item_inventory where owner_id=$ownerId")
                println("Deleted item_inventory of char $rc")
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("delete from actor_position where id=$ownerId")
                println("Deleted actor_position of char $rc")
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("delete from character_stats where char_id=$ownerId")
                println("Deleted character_stats of char $rc")
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("delete from characters where playerid=$playerId")
                println("Deleted characters $rc")
            }
            if (ownedBuildingIds.isNotEmpty()) {
                c.createStatement().use { s ->
                    val rc = s.executeUpdate("delete from item_properties where owner_id in ($buildingIdSet)")
                    println("Deleted item_properties of chests $rc")
                }
                c.createStatement().use { s ->
                    val rc = s.executeUpdate("delete from item_inventory where owner_id in ($buildingIdSet)")
                    println("Deleted item_inventory of chests $rc")
                }
            }
            c.commit()
        }
    }

    override fun compress() {
        conBuilder.connection().use { c ->
            c.createStatement().use { s ->
                val rc = s.executeUpdate("VACUUM")
                println("Execute VACUUM $rc")
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("REINDEX")
                println("Execute REINDEX $rc")
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("ANALYZE")
                println("Execute ANALYZE $rc")
            }
            c.createStatement().use { s ->
                val rc = s.executeUpdate("pragma integrity_check")
                println("Execute pragma integrity_check $rc")
            }
        }
    }

    private fun <T> Connection.readTable(sql: String, buildItem: (ResultSet) -> T): List<T> {
        val l = mutableListOf<T>()
        createStatement().use { s ->
            s.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    l.add(buildItem(rs))
                }
            }
        }
        return l
    }
}