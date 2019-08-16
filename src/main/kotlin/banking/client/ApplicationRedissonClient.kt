package banking.client

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import javax.inject.Singleton

@Singleton
class ApplicationRedissonClient {

    val redissonClientInstance = Redisson.create()

    fun getRedissonClient() : RedissonClient {
        return redissonClientInstance
    }

}