package banking.controller

import banking.Error
import banking.dto.*
import banking.model.AccountStatus
import banking.model.AccountType
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.micronaut.context.ApplicationContext
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.jupiter.api.*
import redis.embedded.RedisServer


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountControllerTest {

    private lateinit var embeddedServer: EmbeddedServer
    private lateinit var client: HttpClient
    lateinit var redisServer: RedisServer
    @BeforeAll
    fun setup() {
        embeddedServer = ApplicationContext.run(EmbeddedServer::class.java)
        client = HttpClient.create(embeddedServer.url)

        redisServer = RedisServer(6379)
        redisServer.start()
    }

    @AfterAll
    fun tearDown() {
        redisServer.stop()
    }


    @Test
    fun `should create account`() {
        val accountDto = AccountDTO(accountBalance = 100.00, customerName = "Anuj Rai", accountType = AccountType.CURRENT)
        val request = HttpRequest.POST("/account/create", accountDto)
        val createdAccount = client.toBlocking().retrieve(request, AccountDTO::class.java)

        createdAccount.accountNumber shouldNotBe null
        createdAccount.baseCurrency shouldBe "INR"
        createdAccount.customerName shouldBe "Anuj Rai"
        createdAccount.status shouldBe AccountStatus.ACTIVE
        createdAccount.accountType shouldBe AccountType.CURRENT

    }

    @Test
    fun `should get account details`() {
        val createdAccount = createDummyUserAccount()

        val request = HttpRequest.GET<Error>("/account/" + createdAccount.accountNumber)
        val fetchedAccountDto = client.toBlocking().retrieve(request, Argument.of(AccountDTO::class.java))

        fetchedAccountDto.accountNumber shouldBe createdAccount.accountNumber
        fetchedAccountDto.baseCurrency shouldBe "INR"
        fetchedAccountDto.customerName shouldBe "Anuj Rai"
    }

    @Test
    fun `should throw bad request if account id is not present`() {
        val request = HttpRequest.GET<Error>("/account/123")
        val accountFetchingException = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request, Argument.of(Map::class.java))
        }

        val responseBody = accountFetchingException.response.body() as Map<String, String>
        accountFetchingException shouldNotBe null
        accountFetchingException.status shouldBe HttpStatus.BAD_REQUEST
        responseBody["errorCode"] shouldBe "ACC.INVALID.001"
        responseBody["errorMessage"] shouldBe "Account Not Found"
    }


    @Test
    fun `should deposit amount in account of User`() {

        val createdAccount = createDummyUserAccount()

        val depositRequest = AccountActivityRequest(
                accountNumber = createdAccount.accountNumber,
                transactionAmount = TransactionAmount(100.00, "INR"),
                activityType = ActivityType.DEPOSIT,
                activityRemark = "Deposit in account"
        )
        val request = HttpRequest.POST("/account/transact", depositRequest)
        val response = client.toBlocking().retrieve(request, AccountActivityResponse::class.java)

        response shouldNotBe null
        response.updatedAccountBalance shouldBe 200.00
        response.status shouldBe ActivityStatus.COMPLETED
        response.message shouldBe "Deposit in account"

        val requestAccountDetail = HttpRequest.GET<Error>("/account/" + createdAccount.accountNumber)
        val fetchedAccountDto = client.toBlocking().retrieve(requestAccountDetail, Argument.of(AccountDTO::class.java))

        fetchedAccountDto.accountBalance shouldBe 200.00
        fetchedAccountDto.accountTransaction.size shouldBe 1
        fetchedAccountDto.accountTransaction[0].transactionType shouldBe ActivityType.DEPOSIT
        fetchedAccountDto.accountTransaction[0].transactionRemark shouldBe "Deposit in account"
        fetchedAccountDto.accountTransaction[0].amount shouldBe 100.00

    }


    @Test
    fun `should withdraw amount from account of User`() {

        val createdAccount = createDummyUserAccount()

        val depositRequest = AccountActivityRequest(
                accountNumber = createdAccount.accountNumber,
                transactionAmount = TransactionAmount(10.00, "INR"),
                activityType = ActivityType.WITHDRAW,
                activityRemark = "Withdraw from account"
        )
        val request = HttpRequest.POST("/account/transact", depositRequest)
        val response = client.toBlocking().retrieve(request, AccountActivityResponse::class.java)

        response shouldNotBe null
        response.updatedAccountBalance shouldBe 90.00
        response.status shouldBe ActivityStatus.COMPLETED
        response.message shouldBe "Withdraw from account"

        val requestAccountDetail = HttpRequest.GET<Error>("/account/" + createdAccount.accountNumber)
        val fetchedAccountDto = client.toBlocking().retrieve(requestAccountDetail, Argument.of(AccountDTO::class.java))

        fetchedAccountDto.accountBalance shouldBe 90.00
        fetchedAccountDto.accountTransaction.size shouldBe 1
        fetchedAccountDto.accountTransaction[0].transactionType shouldBe ActivityType.WITHDRAW
        fetchedAccountDto.accountTransaction[0].transactionRemark shouldBe "Withdraw from account"
        fetchedAccountDto.accountTransaction[0].amount shouldBe 10.00

    }

    @Test
    fun `should give error if withdraw amount more than available balance in account of User`() {

        val createdAccount = createDummyUserAccount()

        val withdrawRequest = AccountActivityRequest(
                accountNumber = createdAccount.accountNumber,
                transactionAmount = TransactionAmount(200.00, "INR"),
                activityType = ActivityType.WITHDRAW,
                activityRemark = "Withdraw from account"
        )
        val request = HttpRequest.POST("/account/transact", withdrawRequest)
        val response = client.toBlocking().retrieve(request, AccountActivityResponse::class.java)



        response shouldNotBe null
        response.updatedAccountBalance shouldBe 0.00
        response.status shouldBe ActivityStatus.ERROR
        response.message shouldBe "Account Balance low"
    }


    private fun createDummyUserAccount(): AccountDTO {
        val accountDto = AccountDTO(accountBalance = 100.00, customerName = "Anuj Rai", accountType = AccountType.SAVINGS)
        val requestCreate = HttpRequest.POST("/account/create", accountDto)
        return client.toBlocking().retrieve(requestCreate, AccountDTO::class.java)
    }

}