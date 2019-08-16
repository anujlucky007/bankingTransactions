package banking.services

import banking.GenericException
import banking.ValidationException
import banking.client.ApplicationRedissonClient
import banking.dao.impl.AccountRepositoryImpl
import banking.dto.*
import banking.model.Account
import banking.model.AccountStatus
import banking.model.AccountType
import io.kotlintest.shouldBe
import io.kotlintest.shouldNot
import io.kotlintest.shouldNotBe
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
        actualException.errorCode shouldBe "ACC.INVALID.001"
        actualException.errorMessage shouldBe "Account Not Found"



        }

    @Test
    fun `should create account User and return Account Number`(){

        val accountDto = AccountDTO(accountBalance=100.00, customerName = "Anuj Rai", accountType = AccountType.SAVINGS)

        val actualAccount=accountService.createAccount(accountDto)

        actualAccount.accountNumber shouldNotBe null
        actualAccount.accountBalance shouldBe 100.00
        actualAccount.accountType shouldBe AccountType.SAVINGS
        actualAccount.status shouldBe AccountStatus.ACTIVE
        actualAccount.baseCurrency shouldBe "INR"

    }

    @Test
    fun `should mark Account activity request as ERROR when account number is not correct`(){

        val accountActivityRequest = AccountActivityRequest(accountNumber = 1001010,activityRemark = "withdraw",transactionAmount = TransactionAmount(110.00,"UNKNOWN"),activityType = ActivityType.WITHDRAW)

        val actualAccountActivityResponse=accountService.doAccountActivity(accountActivityRequest)

        actualAccountActivityResponse.accountNumber shouldBe 1001010
        actualAccountActivityResponse.status shouldBe ActivityStatus.ERROR
    }

    @Test
    fun `should deposit amount in account return update Account balance if currency is same`(){

        val accountActivityRequest = AccountActivityRequest(accountNumber = account.id,activityRemark = "Deposit",transactionAmount = TransactionAmount(1000.00,"INR"),activityType = ActivityType.DEPOSIT)

        val actualAccountActivityResponse=accountService.doAccountActivity(accountActivityRequest)

        actualAccountActivityResponse.accountNumber shouldBe  account.id
        actualAccountActivityResponse.status shouldBe ActivityStatus.COMPLETED

        val actualAccount1=accountService.getAccountDetails(account.id)
        actualAccount1.accountBalance shouldBe 1100.00
    }

    @Test
    fun `should deposit amount in account return update Account balance if deposit currency is in USD and Account base currency is INR`(){

        val accountActivityRequest = AccountActivityRequest(accountNumber = account.id,activityRemark = "Deposit",transactionAmount = TransactionAmount(1000.00,"USD"),activityType = ActivityType.DEPOSIT)

        val actualAccountActivityResponse=accountService.doAccountActivity(accountActivityRequest)

        actualAccountActivityResponse.accountNumber shouldBe account.id
        actualAccountActivityResponse.status shouldBe ActivityStatus.COMPLETED

        val actualAccount1=accountService.getAccountDetails(account.id)
        actualAccount1.accountBalance shouldBe 70100.00
    }

    @Test
    fun `should withdraw amount in account return update Account balance if currency is same`(){

        val accountActivityRequest = AccountActivityRequest(accountNumber = account.id,activityRemark = "withdraw",transactionAmount = TransactionAmount(10.00,"INR"),activityType = ActivityType.WITHDRAW)

        val actualAccountActivityResponse=accountService.doAccountActivity(accountActivityRequest)

        actualAccountActivityResponse.accountNumber shouldBe account.id
        actualAccountActivityResponse.status shouldBe ActivityStatus.COMPLETED

        val actualAccount1=accountService.getAccountDetails(account.id)
        actualAccount1.accountBalance shouldBe 90.00
    }

    @Test
    fun `should mark withdrawl request as ERROR when withdraw amount is More than amount available in account also if currency is same`(){

        val accountActivityRequest = AccountActivityRequest(accountNumber = account.id,activityRemark = "withdraw",transactionAmount = TransactionAmount(110.00,"INR"),activityType = ActivityType.WITHDRAW)

        val actualAccountActivityResponse=accountService.doAccountActivity(accountActivityRequest)

        actualAccountActivityResponse.accountNumber shouldBe account.id
        actualAccountActivityResponse.status shouldBe ActivityStatus.ERROR

        val actualAccount1=accountService.getAccountDetails(account.id)
        actualAccount1.accountBalance shouldBe 100.00
    }

    @Test
    fun `should withdraw amount in account return update Account balance if currency is different`(){

        val accountActivityRequest = AccountActivityRequest(accountNumber = account.id,activityRemark = "withdraw",transactionAmount = TransactionAmount(1.00,"USD"),activityType = ActivityType.WITHDRAW)

        val actualAccountActivityResponse=accountService.doAccountActivity(accountActivityRequest)

        actualAccountActivityResponse.accountNumber shouldBe account.id
        actualAccountActivityResponse.status shouldBe ActivityStatus.COMPLETED

        val actualAccount1=accountService.getAccountDetails(account.id)
        actualAccount1.accountBalance shouldBe 30.00
    }

    @Test
    fun `should mark withdrawl request as ERROR when withdraw amount is More than amount available in account also if currency is different`(){

        val accountActivityRequest = AccountActivityRequest(accountNumber = account.id,activityRemark = "withdraw",transactionAmount = TransactionAmount(110.00,"USD"),activityType = ActivityType.WITHDRAW)

        val actualAccountActivityResponse=accountService.doAccountActivity(accountActivityRequest)

        actualAccountActivityResponse.accountNumber shouldBe account.id
        actualAccountActivityResponse.status shouldBe ActivityStatus.ERROR

        val actualAccount1=accountService.getAccountDetails(account.id)
        actualAccount1.accountBalance shouldBe 100.00
    }

    @Test
    fun `should mark withdrawl request as ERROR when withdraw amount currency is Not Known`(){

        val accountActivityRequest = AccountActivityRequest(accountNumber = account.id,activityRemark = "withdraw",transactionAmount = TransactionAmount(110.00,"UNKNOWN"),activityType = ActivityType.WITHDRAW)

        val actualAccountActivityResponse=accountService.doAccountActivity(accountActivityRequest)

        actualAccountActivityResponse.accountNumber shouldBe account.id
        actualAccountActivityResponse.status shouldBe ActivityStatus.ERROR

        val actualAccount1=accountService.getAccountDetails(account.id)
        actualAccount1.accountBalance shouldBe 100.00
    }




}