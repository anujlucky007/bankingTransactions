package banking.services

import banking.GenericException
import banking.ValidationException
import banking.client.ApplicationRedissonClient
import banking.dao.impl.AccountRepositoryImpl
import banking.model.Account
import banking.model.AccountStatus
import banking.model.AccountType
import io.kotlintest.shouldBe
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import redis.embedded.RedisServer
import javax.inject.Inject

@MicronautTest
class AccountServiceImplTest{

    @Inject
    lateinit var accountRepository: AccountRepositoryImpl

    @Inject
    lateinit var accountService: AccountServiceImpl

    lateinit var  account :Account

    lateinit  var redisServer : RedisServer


    @BeforeEach
    fun setUp() {
        redisServer = RedisServer(6379)
        redisServer.start()

        val acc= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE)
        account=accountRepository.save(acc)
    }

    @AfterEach
    fun tearDown() {
        redisServer.stop()
    }

    @Test
    fun `get account details of User Account`(){

        val actualAccount=accountService.getAccountDetails(account.id)

        actualAccount.id shouldBe account.id
        actualAccount.accountBalance shouldBe 100.00

    }

    @Test
    fun `should throw exception if  details of User Account is not present in DB`(){

        val actualException= Assertions.assertThrows(GenericException::class.java) {
            accountService.getAccountDetails(202)
        }
        actualException.errorCode shouldBe "ACC.INAVLID.001"
        actualException.errorMessage shouldBe "Account Not Found"



        }




}