package banking.services

import banking.dto.AccountActivityRequest
import banking.dto.AccountActivityResponse
import banking.dto.ActivityStatus
import banking.dto.ActivityType
import banking.model.Account
import io.micronaut.spring.tx.annotation.Transactional
import banking.dao.AccountRepository
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
open class AccountServiceImpl(private val lockService: LockService, private val exchangeService: ExchangeService, private val accountRepository: AccountRepository) : AccountService {

    override fun getAccountDetails(accountNumber: String): String {
        val lockOnAccount=lockService.getLockOnAccount(accountNumber)
        //directly fetch from db

        lockService.releaseLockOnAccount(lockOnAccount)
        return "Hello $accountNumber"
    }

    @Transactional
    override fun doAccountActivity(accountActivityRequest: AccountActivityRequest): AccountActivityResponse {

        val accountDetails : Account?
        val distributedAccountLock=lockService.getLockOnAccount(accountActivityRequest.accountNumber.toString())
        distributedAccountLock.lock(20,TimeUnit.SECONDS)
        try {
            accountDetails = accountRepository.findById(accountActivityRequest.accountNumber)

            val amount = exchangeService.convertCurrency(accountActivityRequest.transactionAmount.currency, accountDetails!!.baseCurrency, accountActivityRequest.transactionAmount.value)
            val updatedAccountBalance = when {
                accountActivityRequest.activityType == ActivityType.DEPOSIT -> accountDetails.accountBalance + amount
                else -> {
                    0.0
                }
            }
            accountRepository.updateBalance(accountDetails.id,updatedAccountBalance)
           // accountRepository.createTransaction(accountNumber = accountActivityRequest.accountNumber)

        }
        catch (ex:Exception){
            return AccountActivityResponse(accountNumber = accountActivityRequest.accountNumber, updatedAccountBalance = 0.0, status = ActivityStatus.ERROR)
        }
        finally {
            lockService.releaseLockOnAccount(distributedAccountLock)
        }
        return AccountActivityResponse(accountNumber = accountActivityRequest.accountNumber, updatedAccountBalance = accountDetails!!.accountBalance, status = ActivityStatus.COMPLETED)
    }
}