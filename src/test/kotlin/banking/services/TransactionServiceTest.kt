package banking.services

import banking.GenericException
import banking.ValidationException
import banking.dao.impl.AccountRepositoryImpl
import banking.dto.Creditor
import banking.dto.TransactionActivityStatus
import banking.dto.TransactionRequest
import banking.dto.Value
import banking.model.Account
import banking.model.AccountStatus
import banking.model.AccountType
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.*
import redis.embedded.RedisServer
import java.util.*
import javax.inject.Inject

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionServiceTest{

    @Inject
    lateinit var transactionService: TransactionService

    @Inject
    lateinit var accountRepository: AccountRepositoryImpl

    @Inject
    lateinit var accountService: AccountServiceImpl

    lateinit var  account : Account

    lateinit  var redisServer : RedisServer

    @BeforeAll
    fun setUp() {
        redisServer = RedisServer(6379)
        redisServer.start()

    }

    @AfterAll
    fun tearDown() {
        redisServer.stop()
    }

    @Test
    fun `should transfer from one account to another when both accounts currency is same`(){

        val accountOne= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountOne)

        val accountTwo= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountTwo)


        val transactionRequest = TransactionRequest(
                requestId = "123", creditor = Creditor(bankId = "", accountNumber = accountTwo.id),
                value = Value(50.0, "INR"), description = "Flat rent")

        val transactionResult=transactionService.transactIntraBank(accountOne.id, transactionRequest)

        transactionResult.id shouldNotBe  null
        transactionResult.status shouldBe TransactionActivityStatus.COMPLETED
        transactionResult.message shouldBe "Flat rent"
        transactionResult.value shouldBe Value(50.0,"INR")

        val accountOneUpdated=accountService.getAccountDetails(accountOne.id)
        accountOneUpdated.accountBalance shouldBe 50.0
        accountOneUpdated.accountTransaction.size shouldBe 1

        val accountTwoUpdated=accountService.getAccountDetails(accountTwo.id)
        accountTwoUpdated.accountBalance shouldBe 150.0
        accountTwoUpdated.accountTransaction.size shouldBe 1

    }

    @Test
    fun `should throw ValidationException  when same account transaction is requested`(){

        val accountOne= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountOne)



        val transactionRequest = TransactionRequest(
                requestId = "123", creditor = Creditor(bankId = "", accountNumber = accountOne.id),
                value = Value(50.0, "INR"), description = "Flat rent")


        val transactionException= Assertions.assertThrows(ValidationException::class.java) {
            transactionService.transactIntraBank(accountOne.id, transactionRequest)
        }

        transactionException.errorMessage shouldBe  "Same account transfer"
        transactionException.errorCode shouldBe "OBB.TRANSFER.SAMEACCOUNT"

    }

    @Test
    fun `should mark transaction as failed when currency requested for transaction is not supported`(){

        val accountOne= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountOne)
        val accountTwo= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountTwo)



        val transactionRequest = TransactionRequest(
                requestId = "123", creditor = Creditor(bankId = "", accountNumber = accountTwo.id),
                value = Value(50.0, "UNKNOWN"), description = "Flat rent")

        val transactionResult=transactionService.transactIntraBank(accountOne.id, transactionRequest)

        transactionResult.id shouldNotBe  null
        transactionResult.status shouldBe TransactionActivityStatus.FAILED
        transactionResult.message shouldBe "Debitor : Currency not supported"
        transactionResult.value shouldBe Value(50.0,"UNKNOWN")

        val transactionStatus=transactionService.getTransactionStatus(transactionResult.id)

        transactionStatus.id shouldBe transactionResult.id
        transactionStatus.status shouldBe TransactionActivityStatus.FAILED


    }


    @Test
    fun `should throw exception when same request is sent again for transaction from an account`(){

        val accountOne= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountOne)

        val accountTwo= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountTwo)


        val transactionRequest = TransactionRequest(
                requestId = "123", creditor = Creditor(bankId = "", accountNumber = accountTwo.id),
                value = Value(50.0, "INR"), description = "Flat rent")

       transactionService.transactIntraBank(accountOne.id, transactionRequest)

        val transactionException= Assertions.assertThrows(ValidationException::class.java) {
            transactionService.transactIntraBank(accountOne.id, transactionRequest)
        }

        transactionException.errorMessage shouldBe  "Duplicate Request"
        transactionException.errorCode shouldBe "OBANK.DUPLICATE.002"

        val accountOneUpdated=accountRepository.findById(accountOne.id)
        accountOneUpdated!!.accountBalance shouldBe 50.0

        val accountTwoUpdated=accountRepository.findById(accountTwo.id)
        accountTwoUpdated!!.accountBalance shouldBe 150.0

    }


    @Test
    fun `should transfer from one account to another when both accounts currency is INR but requested transaction is in USD `(){

        val accountOne= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountOne)

        val accountTwo= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountTwo)


        val transactionRequest = TransactionRequest(
                requestId = "123", creditor = Creditor(bankId = "", accountNumber = accountTwo.id),
                value = Value(1.0, "USD"), description = "Flat rent")

        val transactionResult=transactionService.transactIntraBank(accountOne.id, transactionRequest)

        transactionResult.id shouldNotBe  null
        transactionResult.status shouldBe TransactionActivityStatus.COMPLETED
        transactionResult.message shouldBe "Flat rent"
        transactionResult.value shouldBe Value(1.0,"USD")

        val accountOneUpdated=accountService.getAccountDetails(accountOne.id)
        accountOneUpdated.accountBalance shouldBe 30.0
        accountOneUpdated.accountTransaction.size shouldBe 1

        val accountTwoUpdated=accountService.getAccountDetails(accountTwo.id)
        accountTwoUpdated.accountBalance shouldBe 170.0
        accountTwoUpdated.accountTransaction.size shouldBe 1

    }

    @Test
    fun `should  give failed transfer status from one account to another when amount in debitor account is low than requested transfer amount `(){

        val accountOne= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountOne)

        val accountTwo= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountTwo)


        val transactionRequest = TransactionRequest(
                requestId = "123", creditor = Creditor(bankId = "", accountNumber = accountTwo.id),
                value = Value(1000.0, "INR"), description = "Flat rent")

        val transactionResult=transactionService.transactIntraBank(accountOne.id, transactionRequest)

        transactionResult.id shouldNotBe  null
        transactionResult.status shouldBe TransactionActivityStatus.FAILED
        transactionResult.message shouldBe "Debitor : Account Balance low"
        transactionResult.value shouldBe Value(1000.0,"INR")

        val accountOneUpdated=accountService.getAccountDetails(accountOne.id)
        accountOneUpdated.accountBalance shouldBe 100.0
        accountOneUpdated.accountTransaction.size shouldBe 0

        val accountTwoUpdated=accountService.getAccountDetails(accountTwo.id)
        accountTwoUpdated.accountBalance shouldBe 100.0
        accountOneUpdated.accountTransaction.size shouldBe 0

    }

    @Test
    fun `should  give failed transfer status from one account to another when debitor account is wrong `(){

        val transactionRequest = TransactionRequest(
                requestId = "123", creditor = Creditor(bankId = "", accountNumber = 124),
                value = Value(1000.0, "INR"), description = "Flat rent")

        val transactionResult=transactionService.transactIntraBank(123, transactionRequest)

        transactionResult.id shouldNotBe  null
        transactionResult.status shouldBe TransactionActivityStatus.FAILED
        transactionResult.message shouldBe "Debitor : Account Not Found"
        transactionResult.value shouldBe Value(1000.0,"INR")

    }


    @Test
    fun `should  give failed transfer status from one account to another when problem with crediting amount and reverse previous transaction`(){

        val accountOne= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountOne)


        val transactionRequest = TransactionRequest(
                requestId = "123", creditor = Creditor(bankId = "", accountNumber = 1000),
                value = Value(100.0, "INR"), description = "Flat rent")

        val transactionResult=transactionService.transactIntraBank(accountOne.id, transactionRequest)

        transactionResult.id shouldNotBe  null
        transactionResult.status shouldBe TransactionActivityStatus.FAILED
        transactionResult.message shouldBe "Creditor : Account Not Found : Reverse Transaction"
        transactionResult.value shouldBe Value(100.0,"INR")

        val accountOneUpdated=accountService.getAccountDetails(accountOne.id)
        accountOneUpdated.accountBalance shouldBe 100.0
        accountOneUpdated.accountTransaction.size shouldBe 2


    }


    @Test
    fun `should  get status of transaction`(){

        val accountOne= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountOne)

        val accountTwo= Account(id=0,baseCurrency = "INR",accountBalance = 100.00,type = AccountType.CURRENT,status = AccountStatus.ACTIVE,customerName = "ANUJ Rai")
        account=accountRepository.save(accountTwo)


        val transactionRequest = TransactionRequest(
                requestId = "123", creditor = Creditor(bankId = "", accountNumber = accountTwo.id),
                value = Value(50.0, "INR"), description = "Flat rent")

        val actualTransactionResult=transactionService.transactIntraBank(accountOne.id, transactionRequest)

        val transactionStatus=transactionService.getTransactionStatus(actualTransactionResult.id)

        transactionStatus.id shouldBe actualTransactionResult.id
        transactionStatus.status shouldBe TransactionActivityStatus.COMPLETED
        transactionStatus.value shouldBe Value(50.0,"INR")
        transactionStatus.message shouldBe "Flat rent"

    }

    @Test
    fun `should  throw exception  transaction is not Found`(){

        val transactionException=Assertions.assertThrows(GenericException::class.java) {
            transactionService.getTransactionStatus(UUID.randomUUID())
        }

        transactionException.errorCode shouldBe "OBB.TRANSACTION.NOTFOUND"
        transactionException.errorMessage shouldBe "Transaction Not found"

    }

}