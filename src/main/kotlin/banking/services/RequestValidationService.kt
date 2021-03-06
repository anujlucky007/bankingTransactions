package banking.services

import banking.DuplicateException
import banking.ValidationException
import javax.inject.Singleton

@Singleton
class RequestValidationService(var lockService: LockService) {


    fun validateRequest(accountNumber: Long, transactionRequestId: String) : Boolean {
        var lockMapPair = lockService.getLockOnRequestForAccount(accountNumber = accountNumber.toString())
        lockMapPair.first.lock()
        try {
            val requestSet = lockMapPair.second[accountNumber.toString()]
            when {
                requestSet!!.contains(transactionRequestId) -> throw DuplicateException("Request Already Processing or Processed", "OBANK.001")
                else -> {
                    var newRequestSet=requestSet.plus(transactionRequestId)
                    lockMapPair.second[accountNumber.toString()] = newRequestSet
                }
            }
        }
        catch (exception : Exception){
            throw ValidationException("Duplicate Request", "OBANK.DUPLICATE.002")
        }
        finally {
            lockMapPair.first.unlock()
        }
        return true
    }
}