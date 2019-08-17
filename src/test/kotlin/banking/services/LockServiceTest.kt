package banking.services

import banking.client.ApplicationRedissonClient
import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.redisson.Redisson
import org.redisson.api.RLock
import redis.embedded.RedisServer


@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LockServiceTest {

    var applicationRedissonClient = mockkClass(ApplicationRedissonClient::class)

    var lockService = LockService(applicationRedissonClient)

     lateinit  var redisServer : RedisServer

    @BeforeAll
    fun setUp() {
        redisServer = RedisServer(6379)
        redisServer.start()
        val redissonClientInstance = Redisson.create()
        every { applicationRedissonClient.getRedissonClient() } returns redissonClientInstance
    }

    @AfterAll
     fun tearDown() {
        redisServer.stop()
    }

    @Test
    fun `should return Lock on Request Map when accountNumber is not present in requestMap`() {

       var lockMapPair = lockService.getLockOnRequestForAccount("1234")
        lockMapPair shouldNotBe  null
        lockMapPair.second["1234"]!!.size shouldBe 0
        lockMapPair.first shouldBe instanceOf(RLock::class)

    }

    @Test
    fun `should get Lock on Account`() {

        var lockMapPair = lockService.getLockOnAccount("1234")
        lockMapPair shouldNotBe  null
        lockMapPair.isHeldByCurrentThread shouldBe true
        lockMapPair.isLocked shouldBe true
        lockMapPair.unlock()
    }

    @Test
    fun `should release Lock on Account`() {

        var lockMapPair = lockService.getLockOnAccount("1234")

        var lockReleased=lockService.releaseLockOnAccount(lockMapPair)

        lockReleased shouldBe true
        lockMapPair.isHeldByCurrentThread shouldBe false
        lockMapPair.isLocked shouldBe false

    }

}