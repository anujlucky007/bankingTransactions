package banking.dao

import banking.model.Account
import banking.model.AccountStatus
import banking.model.AccountType
import io.kotlintest.shouldBe
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class AccountRepositoryImplTest{

    @Inject
    lateinit var accountRepository: AccountRepositoryImpl


    @Test
    fun `save account in DB`(){

    val acc= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE)

        val account=accountRepository.save(acc)

        val actualAccount=accountRepository.findById(account.id)

        actualAccount!!.accountBalance shouldBe 100.0

        actualAccount.type shouldBe AccountType.CURRENT

        actualAccount.status shouldBe  AccountStatus.ACTIVE

        actualAccount.baseCurrency shouldBe "INR"
    }


}