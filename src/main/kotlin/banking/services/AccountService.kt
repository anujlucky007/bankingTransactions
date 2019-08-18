package banking.services

import banking.dto.AccountActivityRequest
import banking.dto.AccountActivityResponse
import banking.dto.AccountDTO
import banking.model.Account


interface AccountService{

    fun getAccountDetails(accountNumber: Long): AccountDTO

    fun doAccountActivity(accountActivityRequest: AccountActivityRequest): AccountActivityResponse

    fun createAccount(accountCreationRequest: AccountDTO): AccountDTO

    fun closeAccount(accountNumber: Long): AccountDTO
}