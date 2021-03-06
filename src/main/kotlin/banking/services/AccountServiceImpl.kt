package banking.services

import banking.GenericException
import banking.NotExistsException
import banking.model.Account
import io.micronaut.spring.tx.annotation.Transactional
import banking.dao.AccountRepository
import banking.dao.AccountTransactionRepository
import banking.dto.*
import banking.model.AccountStatus
import banking.model.AccountTransaction
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.inject.Singleton

@Singleton
open class AccountServiceImpl(private val lockService: LockService, private val exchangeService: ExchangeService, private val accountRepository: AccountRepository,
                              private val accountTransactionRepository: AccountTransactionRepository
) : AccountService {
    override fun createAccount(accountCreationRequest: AccountDTO): AccountDTO {
        val account= Account(id=0,baseCurrency = accountCreationRequest.baseCurrency,accountBalance = accountCreationRequest.accountBalance,
                type = accountCreationRequest.accountType,status = AccountStatus.ACTIVE,customerName = accountCreationRequest.customerName)
       val createdAccount= accountRepository.save(account)
        return accountCreationRequest.copy(accountNumber =createdAccount.id)
    }

    override fun closeAccount(accountNumber: Long): AccountDTO {
        val account= fetchAccountDetails(accountNumber)
        account.status=AccountStatus.CLOSED
        val updatedAccount= accountRepository.updateAccount(account)

        return AccountDTO(
                accountNumber =accountNumber,
                status = updatedAccount.status,
                accountBalance = updatedAccount.accountBalance,
                baseCurrency = updatedAccount.baseCurrency,
                accountType = updatedAccount.type,
                customerName = updatedAccount.customerName
        )
    }

    override fun getAccountDetails(accountNumber: Long): AccountDTO {

        val accountDetails=fetchAccountDetails(accountNumber)
       val accountTransactions= fetchAccountTransactionDetails(accountNumber)
       var accountTransactionDto= accountTransactions.stream().map {
            AccountTransactionDTO(id=it.id!!,transactionType = it.transactionType,amount = it.amount,transactionRemark = it.transactionRemark)
        }.collect(Collectors.toList())

        return AccountDTO(accountNumber= accountDetails.id,customerName=accountDetails.customerName,accountBalance = accountDetails.accountBalance
                ,baseCurrency = accountDetails.baseCurrency,accountType = accountDetails.type,status = accountDetails.status,accountTransaction =accountTransactionDto)
    }

    private fun fetchAccountDetails(accountNumber: Long): Account {
        return accountRepository.findById(accountNumber) ?: throw NotExistsException("Account Not Found","ACC.INVALID.001")

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
            when(accountDetails.status ) {
                AccountStatus.CLOSED -> throw NotExistsException("Account closed","OBB.ACCOUNT.CLOSED")
                AccountStatus.ACTIVE -> {
                    val amountInAccountBaseCurrency = exchangeService.convertCurrency(accountActivityRequest.transactionAmount.currency, accountDetails.baseCurrency, accountActivityRequest.transactionAmount.value)
                    val updatedAccountBalance = getNewAccountBalance(accountActivityRequest, accountDetails, amountInAccountBaseCurrency)
                    accountDetails.accountBalance = updatedAccountBalance
                    val accountTransactional = AccountTransaction(transactionRemark = accountActivityRequest.activityRemark, transactionType = accountActivityRequest.activityType, amount = amountInAccountBaseCurrency, account = accountDetails)
                    accountRepository.updateAccount(accountDetails)
                    accountTransactionRepository.save(accountTransactional)
                }
            }
        }
        catch (ex:NotExistsException){
            return AccountActivityResponse(accountNumber = accountActivityRequest.accountNumber, updatedAccountBalance = 0.0, status = ActivityStatus.ERROR,message = ex.errorMessage)
        }
        catch (ex:Exception){
            return AccountActivityResponse(accountNumber = accountActivityRequest.accountNumber, updatedAccountBalance = 0.0, status = ActivityStatus.ERROR,message = ex.message)
        }
        finally {
            lockService.releaseLockOnAccount(distributedAccountLock)
        }
        return AccountActivityResponse(accountNumber = accountActivityRequest.accountNumber, updatedAccountBalance = accountDetails!!.accountBalance, status = ActivityStatus.COMPLETED,message =accountActivityRequest.activityRemark)
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