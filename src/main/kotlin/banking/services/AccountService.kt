package banking.services

import banking.dto.AccountActivityRequest
import banking.dto.AccountActivityResponse


interface AccountService{

    fun getAccountDetails(accountNumber: String): String

    fun doAccountActivity(accountActivityRequest: AccountActivityRequest): AccountActivityResponse
}