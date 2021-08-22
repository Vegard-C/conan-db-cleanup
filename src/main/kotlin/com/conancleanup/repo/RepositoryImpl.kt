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
            return c.readTable("select playerId, char_name, lastTimeOnline, guild from characters") { rs ->
                val guild: Any? = rs.getObject(4)
                PlayerEO(
                    id = rs.getLong(1),
                    name = rs.getString(2),
                    lastOnlineEpocheSeconds = rs.getLong(3),
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