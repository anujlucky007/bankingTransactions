package banking.services

import banking.model.Account
import io.micronaut.spring.tx.annotation.Transactional
import banking.dao.AccountRepository
import banking.dto.*
import banking.model.AccountStatus
import banking.model.AccountType
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
open class AccountServiceImpl(private val lockService: LockService, private val exchangeService: ExchangeService, private val accountRepository: AccountRepository) : AccountService {
    override fun createAccount(accountCreationRequest: AccountDTO): AccountDTO {
        val account= Account(id=0,baseCurrency = accountCreationRequest.baseCurrency,accountBalance = accountCreationRequest.accountBalance,
                type = accountCreationRequest.accountType,status = AccountStatus.ACTIVE)
       val createdAccount= accountRepository.save(account)
        return accountCreationRequest.copy(accountNumber =createdAccount.id)
    }

    override fun getAccountDetails(accountNumber: Long): Account {
        val lockOnAccount=lockService.getLockOnAccount(accountNumber.toString())
        //directly fetch from db
        val account=accountRepository.findById(accountNumber.toLong())

        lockService.releaseLockOnAccount(lockOnAccount)

        return account!!

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