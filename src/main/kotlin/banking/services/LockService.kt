package banking.services

import banking.client.ApplicationRedissonClient
import org.redisson.api.RLock
import org.redisson.api.RMap
import javax.inject.Singleton

@Singleton
class LockService(val applicationRedissonClient: ApplicationRedissonClient) {


    fun  getLockOnRequestForAccount(accountNumber:String) : Pair<RLock,RMap<String,Set<String>>> {

        val requestMap= applicationRedissonClient.getRedissonClient().getMap<String, Set<String>>("requestMap")
        if(!requestMap.containsKey(accountNumber)){
            val requestSet=HashSet<String>()
            requestMap.fastPutIfAbsent(accountNumber,requestSet)
        }
        return Pair(requestMap.getFairLock(accountNumber),requestMap)
        }

    fun  getLockOnAccount(accountNumber:String) : RLock {

        val requestMap= applicationRedissonClient.getRedissonClient().getMap<String, Any>("accountMap")
        if(!requestMap.containsKey(accountNumber)){
            val lockObject=Object()
            requestMap.fastPutIfAbsent(accountNumber,lockObject)
        }
        val lock=requestMap.getFairLock(accountNumber)
        lock.lock()
        return lock
    }

    fun  releaseLockOnAccount(lock:RLock) : Boolean {
        return when {
            lock.isHeldByCurrentThread -> {
                lock.unlock()
                true
            }
            else -> false
        }
    }

}