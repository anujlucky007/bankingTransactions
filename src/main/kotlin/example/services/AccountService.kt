package banking.services

import example.services.LockService
import javax.inject.Singleton

@Singleton
class AccountService(val lockService: LockService) {

    fun getAccountDetails(accountNumber: String): String {
        val lockOnAccount=lockService.getLockOnAccount(accountNumber)
        //directly fetch from db

        lockService.releaseLockOnAccount(lockOnAccount)
        return "Hello $accountNumber"
    }

    fun addBalance(accountNumber: Int): String {
        // lock on account before adding balance

        return "Hello $accountNumber"
    }

    fun withdrawBalance(accountNumber: Int): String {
        // lock on account before adding balance

        return "Hello $accountNumber"
    }
}