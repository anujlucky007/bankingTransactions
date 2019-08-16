package banking.dao

import banking.dao.impl.AccountRepositoryImpl
import banking.dao.impl.AccountTransactionRepositoryImpl
import banking.dto.ActivityType
import banking.model.*
import io.kotlintest.shouldBe
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject


@MicronautTest
class AccountTransactionRepositoryImplTest{
    @Inject
    lateinit var accountTransactionRepository: AccountTransactionRepositoryImpl

    @Inject
    lateinit var accountRepository: AccountRepositoryImpl


    @Test
    fun `save account in DB`(){

        val acc= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE)

        val account=accountRepository.save(acc)

        val accTransaction= AccountTransaction(id=0,transactionRemark = "deposit",amount = 100.00,transactionType = ActivityType.DEPOSIT,account =account)

        val accountTransaction=accountTransactionRepository.save(accTransaction)

        val actualAccountTransaction=accountTransactionRepository.findById(accountTransaction.id)

        actualAccountTransaction!!.account.id shouldBe account.id
        actualAccountTransaction.amount shouldBe 100.00
        actualAccountTransaction.transactionType shouldBe ActivityType.DEPOSIT
        actualAccountTransaction.transactionRemark shouldBe "deposit"


    }

}