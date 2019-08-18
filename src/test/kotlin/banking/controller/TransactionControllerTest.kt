package banking.controller

import banking.Error
import banking.dto.*
import banking.model.AccountType
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.micronaut.context.ApplicationContext
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import redis.embedded.RedisServer
import java.util.*

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionControllerTest {
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
    fun `should transfer amount from one account to another of same bank`() {

        val createdAccountOne = createDummyUserAccount()

        val createdAccountTwo = createDummyUserAccount()

        val transactionRequest = TransactionRequest(
                requestId = "123",
                value = Value(100.00, "INR"),
                description = "account transfer",
                creditor = Creditor(accountNumber = createdAccountTwo.accountNumber)
        )
        //Transfer Api call
        val request = HttpRequest.POST("/transaction/${createdAccountOne.accountNumber}/transaction-request/${TransactionType.INTRABANK}", transactionRequest)
        val response = client.toBlocking().retrieve(request, TransactionResponse::class.java)

        response shouldNotBe null
        response.id shouldNotBe null
        response.value shouldBe Value(100.00, "INR")
        response.status shouldBe TransactionActivityStatus.COMPLETED
        response.message shouldBe "account transfer"

        //fetching Account one details
        val requestAccountDetail = HttpRequest.GET<AccountDTO>("/account/" + createdAccountOne.accountNumber)
        val fetchedAccountDto = client.toBlocking().retrieve(requestAccountDetail, Argument.of(AccountDTO::class.java))

        fetchedAccountDto.accountBalance shouldBe 0.00
        fetchedAccountDto.accountTransaction.size shouldBe 1
        fetchedAccountDto.accountTransaction[0].transactionType shouldBe ActivityType.WITHDRAW
        fetchedAccountDto.accountTransaction[0].transactionRemark shouldBe "WITHDRAW for depositing in account ${createdAccountTwo.accountNumber}"
        fetchedAccountDto.accountTransaction[0].amount shouldBe 100.00


        val requestAccountDetailSecond = HttpRequest.GET<AccountDTO>("/account/${createdAccountTwo.accountNumber}")
        val fetchedAccountSecondDto = client.toBlocking().retrieve(requestAccountDetailSecond, Argument.of(AccountDTO::class.java))

        fetchedAccountSecondDto.accountBalance shouldBe 200.00
        fetchedAccountSecondDto.accountTransaction.size shouldBe 1
        fetchedAccountSecondDto.accountTransaction[0].transactionType shouldBe ActivityType.DEPOSIT
        fetchedAccountSecondDto.accountTransaction[0].transactionRemark shouldBe "DEPOSIT from account ${createdAccountOne.accountNumber}"
        fetchedAccountSecondDto.accountTransaction[0].amount shouldBe 100.00


        //fetching transaction status using transaction id generated
        val transactionStatusRequest = HttpRequest.GET<TransactionResponse>("/transaction/${response.id}/status")
        val transactionStatus = client.toBlocking().retrieve(transactionStatusRequest, Argument.of(TransactionResponse::class.java))

        transactionStatus.status shouldBe TransactionActivityStatus.COMPLETED
        transactionStatus.message shouldBe "account transfer"
        transactionStatus.value shouldBe Value(amount = 100.0, currency = "INR")

    }


    @Test
    fun `should give Error in transfer from one account to another of same bank when services give error`() {

        val createdAccountOne = createDummyUserAccount()

        val createdAccountTwo = createDummyUserAccount()

        val transactionRequest = TransactionRequest(
                requestId = "123",
                value = Value(1000.00, "INR"),
                description = "account transfer",
                creditor = Creditor(accountNumber = createdAccountTwo.accountNumber)
        )
        //Transfer Api call

        val request = HttpRequest.POST("/transaction/${createdAccountOne.accountNumber}/transaction-request/${TransactionType.INTRABANK}", transactionRequest)
        val response = client.toBlocking().retrieve(request, TransactionResponse::class.java)

        response shouldNotBe null
        response.id shouldNotBe null
        response.value shouldBe Value(1000.00, "INR")
        response.status shouldBe TransactionActivityStatus.FAILED
        response.message shouldBe "Debitor : Account Balance low"


        //fetching Account one details
        val requestAccountDetail = HttpRequest.GET<AccountDTO>("/account/" + createdAccountOne.accountNumber)
        val fetchedAccountDto = client.toBlocking().retrieve(requestAccountDetail, Argument.of(AccountDTO::class.java))

        fetchedAccountDto.accountBalance shouldBe 100.00
        fetchedAccountDto.accountTransaction.size shouldBe 0


        //fetching Account two details
        val requestAccountDetailSecond = HttpRequest.GET<AccountDTO>("/account/${createdAccountTwo.accountNumber}")
        val fetchedAccountSecondDto = client.toBlocking().retrieve(requestAccountDetailSecond, Argument.of(AccountDTO::class.java))

        fetchedAccountSecondDto.accountBalance shouldBe 100.00
        fetchedAccountSecondDto.accountTransaction.size shouldBe 0


        //fetching transaction status using transaction id generated
        val transactionStatusRequest = HttpRequest.GET<TransactionResponse>("/transaction/${response.id}/status")
        val transactionStatus = client.toBlocking().retrieve(transactionStatusRequest, Argument.of(TransactionResponse::class.java))

        transactionStatus.status shouldBe TransactionActivityStatus.FAILED
        transactionStatus.message shouldBe "Debitor : Account Balance low"
        transactionStatus.value shouldBe Value(amount = 1000.0, currency = "INR")

    }


    @Test
    fun `should transfer amount from one account to another of Different bank`() {

        val createdAccountOne = createDummyUserAccount()

        val createdAccountTwo = createDummyUserAccount()

        val transactionRequest = TransactionRequest(
                requestId = "123",
                value = Value(100.00, "INR"),
                description = "account transfer",
                creditor = Creditor(accountNumber = createdAccountTwo.accountNumber)
        )
        val request = HttpRequest.POST("/transaction/${createdAccountOne.accountNumber}/transaction-request/${TransactionType.INTERBANK}", transactionRequest)
        val response = client.toBlocking().retrieve(request, TransactionResponse::class.java)

        response shouldNotBe null
        response.id shouldNotBe null
        response.value shouldBe Value(0.00, "XYZ")
        response.status shouldBe TransactionActivityStatus.ERROR
        response.message shouldBe "FUNCTIONALITY NOT SUPPORTED"
    }

    private fun createDummyUserAccount(): AccountDTO {
        val accountDto = AccountDTO(accountBalance = 100.00, customerName = "Anuj Rai", accountType = AccountType.SAVINGS)
        val requestCreate = HttpRequest.POST("/account/create", accountDto)
        return client.toBlocking().retrieve(requestCreate, AccountDTO::class.java)
    }


}