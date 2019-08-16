package banking.services

import banking.GenericException
import banking.model.Account
import io.micronaut.spring.tx.annotation.Transactional
import banking.dao.AccountRepository
import banking.dao.AccountTransactionRepository
import banking.dto.*
import banking.model.AccountStatus
import banking.model.AccountTransaction
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
open class AccountServiceImpl(private val lockService: LockService, private val exchangeService: ExchangeService, private val accountRepository: AccountRepository,
                              private val accountTransactionRepository: AccountTransactionRepository
) : AccountService {
    override fun createAccount(accountCreationRequest: AccountDTO): AccountDTO {
        val account= Account(id=0,baseCurrency = accountCreationRequest.baseCurrency,accountBalance = accountCreationRequest.accountBalance,
                type = accountCreationRequest.accountType,status = AccountStatus.ACTIVE)
       val createdAccount= accountRepository.save(account)
        return accountCreationRequest.copy(accountNumber =createdAccount.id)
    }

    override fun getAccountDetails(accountNumber: Long): Account {
        return fetchAccountDetails(accountNumber)

    }

    private fun fetchAccountDetails(accountNumber: Long): Account {
        return accountRepository.findById(accountNumber) ?: throw GenericException("Account Not Found","ACC.INVALID.001")

    }

    private fun fetchAccountTransactionDetails(accountNumber: Long): List<AccountTransaction> {
        return accountTransactionRepository.findTransactionsOfAccount(accountNumber)

    }

    @Transactional
    override fun doAccountActivity(accountActivityRequest: AccountActivityRequest): AccountActivityResponse {

        val accountDetails : Account?
        val distributedAccountLock=lockService.getLockOnAccount(accountActivityRequest.accountNumber.toString())
        distributedAccountLock.lock(20,TimeUnit.SECONDS)
        try {
            accountDetails = fetchAccountDetails(accountActivityRequest.accountNumber)
            val amountInAccountBaseCurrency = exchangeService.convertCurrency(accountActivityRequest.transactionAmount.currency, accountDetails!!.baseCurrency, accountActivityRequest.transactionAmount.value)
            val updatedAccountBalance = getNewAccountBalance(accountActivityRequest, accountDetails, amountInAccountBaseCurrency)
            accountDetails.accountBalance=updatedAccountBalance
            val accountTransactional=AccountTransaction(transactionRemark = accountActivityRequest.activityRemark,transactionType = accountActivityRequest.activityType,amount = amountInAccountBaseCurrency,account =accountDetails)
            accountRepository.updateBalance(accountDetails.id,accountDetails)
            accountTransactionRepository.save(accountTransactional)
        }
        catch (ex:Exception){
            return AccountActivityResponse(accountNumber = accountActivityRequest.accountNumber, updatedAccountBalance = 0.0, status = ActivityStatus.ERROR,message = ex.message)
        }
        finally {
            lockService.releaseLockOnAccount(distributedAccountLock)
        }
        return AccountActivityResponse(accountNumber = accountActivityRequest.accountNumber, updatedAccountBalance = accountDetails!!.accountBalance, status = ActivityStatus.COMPLETED,message = "")
    }

    private fun getNewAccountBalance(accountActivityRequest: AccountActivityRequest, accountDetails: Account, amount: Double): Double {
        return when (accountActivityRequest.activityType) {
            ActivityType.DEPOSIT -> accountDetails.accountBalance + amount
            ActivityType.WITHDRAW -> {
                when {
                    accountDetails.accountBalance < amount -> throw GenericException("Account Balance low", "OBB.ACC.BALANCE")
                    else -> accountDetails.accountBalance - amount
                }
            }
        }
    }
}