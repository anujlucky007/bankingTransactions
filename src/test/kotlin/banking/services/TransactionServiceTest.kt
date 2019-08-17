package banking.services

import banking.dao.impl.AccountRepositoryImpl
import banking.model.Account
import banking.model.AccountStatus
import banking.model.AccountType
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.*
import redis.embedded.RedisServer
import javax.inject.Inject

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionServiceTest{

    @Inject
    lateinit var transactionService: TransactionService

    @Inject
    lateinit var accountRepository: AccountRepositoryImpl

    lateinit var  account : Account

    lateinit  var redisServer : RedisServer

    @BeforeAll
    fun setUp() {
        redisServer = RedisServer(6379)
        redisServer.start()

        val accountOne= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountOne)

        val accountTwo= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountTwo)

    }

    @AfterAll
    fun tearDown() {
        redisServer.stop()
    }

    @Test
    fun `check`(){

    }

}