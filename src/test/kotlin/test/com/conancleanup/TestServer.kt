package test.com.conancleanup

import com.conancleanup.Account
import com.conancleanup.Service
import com.conancleanup.repo.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class TestServer {

    @Test
    fun `accounts ordered by ID asc`() {
        fun accountsListTesting(iDs: List<Long>) {
            val testRepo = TestConanRepository(accounts = iDs.map { AccountEO(it, "xy") })
            val test = Service(testRepo)
            val sortedAccounts = test.readServer().accounts()
            assertThat(sortedAccounts.map { it.id }).isEqualTo(iDs.sorted())
        }

        accountsListTesting(listOf(5L, 2L, 3L, 1L, 9L))
        accountsListTesting(listOf())
        accountsListTesting(listOf(100L, 20L, 70L, 30L, 50L))
        accountsListTesting(listOf(30L, 30L, 30L))
    }

    @Test
    fun `guilds ordered by name asc`() {
        fun guildsListTesting(names: List<String>) {
            val testRepo = TestConanRepository(guilds = names.map { GuildEO(1, it) })
            val test = Service(testRepo)
            val sortedNames = test.readServer().guilds()
            assertThat(sortedNames.map { it.name }).isEqualTo(names.sorted())
        }

        guildsListTesting(listOf("Verg", "Prukrot", "Jardarn"))
        guildsListTesting(listOf())
        guildsListTesting(listOf("Verg", "Verg", "Verg"))
        guildsListTesting(listOf("Prati", "Askotris", "Qwyrinzum", "Sepalli"))
    }

    @Test
    fun `players ordered by ID and name asc`() {
        fun playersListTesting(nameAndID: List<Pair<String, String>>) {
            val testRepo = TestConanRepository(
                accounts = nameAndID.mapIndexed { index, (_, funcom) ->
                    AccountEO(
                        index.toLong(),
                        funcom
                    )
                },
                players = nameAndID.mapIndexed { index, (name, _) ->
                    PlayerEO(
                        index.toLong(),
                        0L,
                        name,
                        0L
                    )
                }
            )
            val test = Service(testRepo)
            val players = test.readServer().players()
            val sortedNamesAndIDs = nameAndID.map { (name, funcomId) -> name + funcomId }.sorted()
            assertThat(players.map { it.name + it.account.funcomId }).isEqualTo(sortedNamesAndIDs)
        }

        playersListTesting(listOf(Pair("alf", "blf"), Pair("alf", "alf"), Pair("abc", "alf")))
        playersListTesting(listOf(Pair("rudolf", "balef"), Pair("ralof", "tralef"), Pair("abico", "alfon")))
        playersListTesting(listOf(Pair("asfdflsdff", "bclvbfcb"), Pair("ergalergf", "aerglegf"), Pair("uirabic", "alcorf")))
    }

    class TestConanRepository(
        private val accounts: Collection<AccountEO> = listOf(),
        private val guilds: Collection<GuildEO> = listOf(),
        private val players: Collection<PlayerEO> = listOf(),
    ) : ConanRepository {
        override fun readAccounts(): Collection<AccountEO> = accounts
        override fun readGuilds(): Collection<GuildEO> = guilds
        override fun readPlayers(): Collection<PlayerEO> = players
        override fun readBuildingsAndPlaceables(): Collection<BuildingOrPlaceableEO> = listOf()
        override fun readBuildingInstances(): Collection<BuildingInstancesEO> = listOf()
        override fun updateOwnership(buildingIds: List<Long>, owner: Long) {
            TODO("Not yet implemented")
        }

        override fun deletePlayer(playerId: Long, ownerId: Long) {
            TODO("Not yet implemented")
        }

        override fun compress() {
            TODO("Not yet implemented")
        }

        override fun deleteGuild(guildId: Long) {
            TODO("Not yet implemented")
        }
    }
}