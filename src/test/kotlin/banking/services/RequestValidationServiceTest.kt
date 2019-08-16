package banking.services

import banking.ValidationException
import banking.client.ApplicationRedissonClient
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import redis.embedded.RedisServer

@ExtendWith(MockKExtension::class)
class RequestValidationServiceTest{


    private lateinit var requestValidationService: RequestValidationService
    private lateinit var lockService: LockService
    private lateinit var applicationRedissonClient: ApplicationRedissonClient
    lateinit  var redisServer : RedisServer

    @BeforeEach
    fun setUp() {
        redisServer = RedisServer(6379)
        redisServer.start()
         applicationRedissonClient = ApplicationRedissonClient()
         lockService = LockService(applicationRedissonClient)
         requestValidationService = RequestValidationService(lockService)
    }

    @AfterEach
    fun tearDown() {
        redisServer.stop()
    }

    @Test
    fun `should return true on when requestId is not present in requestMap for account`() {

        var lock = requestValidationService.validateRequest(1234,"tx1")
        lock shouldNotBe  null
        lock shouldBe true
    }

    @Test
    fun `should throw exception when requestId is duplicate for account Number`() {

        var lock = requestValidationService.validateRequest(1234,"tx1")
        lock shouldNotBe  null
        lock shouldBe true
        val exception=assertThrows(ValidationException::class.java) { requestValidationService.validateRequest(1234,"tx1") }
        exception.errorCode shouldBe "OBANK.002"
        exception.errorMessage shouldBe "Request Not Processed Something Went Wrong"
    }

}