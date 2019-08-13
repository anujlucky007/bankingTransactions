package example.services

import example.client.ApplicationRedissonClient
import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.redisson.Redisson
import org.redisson.api.RLock
import redis.embedded.RedisServer


@ExtendWith(MockKExtension::class)
class LockServiceTest {

    var applicationRedissonClient = mockkClass(ApplicationRedissonClient::class)

    var lockService = LockService(applicationRedissonClient)

     lateinit  var redisServer : RedisServer

    @BeforeEach
    fun setUp() {
        redisServer = RedisServer(6379)
        redisServer.start()
        val redissonClientInstance = Redisson.create()
        every { applicationRedissonClient.getRedissonClient() } returns redissonClientInstance
    }

    @AfterEach
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

}