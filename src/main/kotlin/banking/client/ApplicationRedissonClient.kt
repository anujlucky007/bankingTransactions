package banking.client

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import javax.inject.Singleton

@Singleton
open class ApplicationRedissonClient {

    var redissonClientInstance :RedissonClient? =null

    fun getRedissonClient() : RedissonClient {
        when (redissonClientInstance) {
            null -> redissonClientInstance= Redisson.create()
        }
        return redissonClientInstance!!
    }

}