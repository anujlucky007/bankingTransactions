package banking.services

import banking.GenericException
import banking.ValidationException
import banking.dao.CustomerTransactionRepository
import banking.dto.*
import banking.model.CustomerTransaction
import java.lang.Exception
import java.util.*
import javax.inject.Singleton


@Singleton
class TransactionService(var requestValidationService: RequestValidationService, private val accountService: AccountServiceImpl,val customerTransactionRepository: CustomerTransactionRepository) {

    fun getTransactionStatus(id: UUID) :TransactionResponse {
        val customerTransaction= customerTransactionRepository.findById(id) ?: throw GenericException("Transaction Not found","OBB.TRANSACTION.NOTFOUND")

        return TransactionResponse(
                id=customerTransaction.id!!,
                status = customerTransaction.status,
                value = Value(customerTransaction.amount,customerTransaction.currency)
        )
    }

    fun transactIntraBank(accountNumber: Long, transactionRequest: TransactionRequest): TransactionResponse {

        requestValidationService.validateRequest(accountNumber, transactionRequest.requestId)

        if (accountNumber == transactionRequest.creditor.accountNumber) {
            throw ValidationException("Same account transfer", "OBB.TRANSFER.SAMEACCOUNT")
        }

        val customerAccountTransferTransaction = createNewCustomerTransaction(transactionRequest, accountNumber)

        try {
            val accountWithdrawalActivityRequest = AccountActivityRequest(accountNumber = accountNumber,
                    transactionAmount = TransactionAmount(transactionRequest.value.amount, transactionRequest.value.currency),
                    activityRemark = "${ActivityType.WITHDRAW} for depositing in account ${transactionRequest.creditor.accountNumber}",
                    activityType = ActivityType.WITHDRAW)

            val accountDebitActivityResponse = accountService.doAccountActivity(accountWithdrawalActivityRequest)

            return when (accountDebitActivityResponse.status) {
                ActivityStatus.COMPLETED -> {
                    creditAmountInAccount(transactionRequest, accountNumber, customerAccountTransferTransaction)
                }
                ActivityStatus.ERROR -> {
                    updateCustomerTransaction(
                            customerTransaction = customerAccountTransferTransaction,
                            value = transactionRequest.value,
                            message = "Debitor : " + accountDebitActivityResponse.message!!,
                            status = TransactionActivityStatus.FAILED
                    )
                }
            }
        } catch (ex: Exception) {
            return updateCustomerTransaction(
                    customerTransaction = customerAccountTransferTransaction,
                    value = transactionRequest.value,
                    message = ex.message.toString(),
                    status = TransactionActivityStatus.ERROR
            )

        }
    }

    private fun creditAmountInAccount(transactionRequest: TransactionRequest, debitAccountNumber: Long, createCustomerTransaction:CustomerTransaction): TransactionResponse {
        val accountDepositActivityRequest = AccountActivityRequest(accountNumber = transactionRequest.creditor.accountNumber,
                transactionAmount = TransactionAmount(transactionRequest.value.amount, transactionRequest.value.currency),
                activityRemark = "${ActivityType.DEPOSIT} from account $debitAccountNumber",
                activityType = ActivityType.DEPOSIT)

        val accountCreditActivityResponse = accountService.doAccountActivity(accountDepositActivityRequest)
        return when (accountCreditActivityResponse.status) {
            ActivityStatus.ERROR -> {
                reverseTransaction(debitAccountNumber, transactionRequest)
                updateCustomerTransaction(
                        customerTransaction = createCustomerTransaction,
                        value = transactionRequest.value,
                        message = "Creditor : ${accountCreditActivityResponse.message} : Reverse Transaction",
                        status = TransactionActivityStatus.FAILED
                )
            }
            ActivityStatus.COMPLETED -> {
                updateCustomerTransaction(
                        customerTransaction = createCustomerTransaction,
                        value = transactionRequest.value,
                        message = transactionRequest.description,
                        status = TransactionActivityStatus.COMPLETED
                )
            }
        }
    }

    private fun reverseTransaction(accountNumber: Long, transactionRequest: TransactionRequest) {
        val accountReverseDepositActivityRequest = AccountActivityRequest(accountNumber = accountNumber,
                transactionAmount = TransactionAmount(transactionRequest.value.amount, transactionRequest.value.currency),
                activityRemark = "Reverse Transaction",
                activityType = ActivityType.DEPOSIT)

        accountService.doAccountActivity(accountReverseDepositActivityRequest)
    }

    private fun createNewCustomerTransaction(transactionRequest: TransactionRequest, accountNumber: Long): CustomerTransaction {
        var createCustomerTransaction = CustomerTransaction(creditorAccountNumber = transactionRequest.creditor.accountNumber,
                debitorAccountNumber = accountNumber, status = TransactionActivityStatus.INPROGRESS,currency = transactionRequest.value.currency,
                amount = transactionRequest.value.amount
                )

        createCustomerTransaction = customerTransactionRepository.save(createCustomerTransaction)
        return createCustomerTransaction
    }

    private fun updateCustomerTransaction(customerTransaction: CustomerTransaction,value:Value,status :TransactionActivityStatus, message:String ): TransactionResponse {

        customerTransaction.status=status
        customerTransaction.message=message

        customerTransactionRepository.updateCustomerTransaction(customerTransaction = customerTransaction)

        return TransactionResponse(
                id=customerTransaction.id!!,
                status = customerTransaction.status,
                message = message,
                value = value
        )
    }
}
