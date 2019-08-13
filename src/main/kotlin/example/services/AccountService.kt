package banking.services

import javax.inject.Singleton

@Singleton
class AccountService {

    fun getAccountDetails(accountNumber: Int): String {
        //directly fetch from db
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