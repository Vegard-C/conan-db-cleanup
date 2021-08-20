package test.com.conancleanup

import com.conancleanup.Service
import com.conancleanup.repo.AccountEO
import com.conancleanup.repo.ConanRepository
import com.conancleanup.repo.GuildEO
import com.conancleanup.repo.PlayerEO
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class TestServer {

    @Test
    fun `accounts ordered by ID asc`() {
        accountsListTesting(listOf(5L,2L,3L,1L,9L))
        accountsListTesting(listOf())
        accountsListTesting(listOf(100L,20L,70L,30L,50L))
        accountsListTesting(listOf(30L,30L,30L))
    }

    @Test
    fun `guilds ordered by name asc`() {
        guildsListTesting(listOf("Verg","Prukrot","Jardarn"))
        guildsListTesting(listOf())
        guildsListTesting(listOf("Verg","Verg","Verg"))
        guildsListTesting(listOf("Prati","Askotris","Qwyrinzum","Sepalli"))
    }

    private fun accountsListTesting(iDs: List<Long>){
        val testRepo = AccountsTestConanRepository(iDs)
        val test = Service(testRepo)
        val sortedAccounts = test.readServer().accounts()
        assertThat(sortedAccounts.map { it.id }).isEqualTo(iDs.sorted())
    }

    private fun guildsListTesting(names: List<String>){
        val testRepo = GuildsTestConanRepository(names)
        val test = Service(testRepo)
        val sortedNames = test.readServer().guilds()
        assertThat(sortedNames.map { it.name }).isEqualTo(names.sorted())
    }

    class AccountsTestConanRepository(private val accIds: List<Long>): ConanRepository{
        override fun readAccounts(): Collection<AccountEO> = accIds.map { AccountEO(it, "xy") }
        override fun readGuilds(): Collection<GuildEO> = listOf()
        override fun readPlayers(): Collection<PlayerEO> = listOf()
    }

    class GuildsTestConanRepository(private val names: List<String>): ConanRepository{
        override fun readAccounts(): Collection<AccountEO> = listOf()
        override fun readGuilds(): Collection<GuildEO> = names.map { GuildEO(1, it)}
        override fun readPlayers(): Collection<PlayerEO> = listOf()
    }
}