package example.services

import banking.model.TransactionRequest
import example.client.ApplicationRedissonClient
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

}