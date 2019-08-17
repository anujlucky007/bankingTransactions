package banking.services

import banking.GenericException
import banking.dao.CustomerTransactionRepository
import banking.dto.*
import banking.model.CustomerTransaction
import java.lang.Exception
import java.util.*
import javax.inject.Singleton


@Singleton
class TransactionService(var requestValidationService: RequestValidationService, private val accountService: AccountServiceImpl,val customerTransactionRepository: CustomerTransactionRepository) {

    // 1. is transaction request valid
    //2. generate transaction id
    //3. save transaction in db (id, txn creditor remark, txn debitor remark, creditor account id , amount , currency, status )
    //4. validate account balance with curency into consideration
    //5. debit from account
    //6 credit to account -- should return some object not exception
    //send response back with transaction id generated
    //transactional property with debit and credit
    //6 credit to account -- should return some object not exception
    //send response back with transaction id generated
    //transactional property with debit and credit

    fun getTransactionStatus(id: UUID) :TransactionResponse {
        val customerTransaction= customerTransactionRepository.findById(id) ?: throw GenericException("Transaction Not found","OBB.TRANSACTION.NOTFOUND")

        return TransactionResponse(id=customerTransaction.id!!,status = customerTransaction.status,value = Value(customerTransaction.amount,customerTransaction.currency))
    }

    fun transactIntraBank(accountNumber: Long, transactionRequest: TransactionRequest): TransactionResponse {

        requestValidationService.validateRequest(accountNumber, transactionRequest.requestId)

        var createCustomerTransaction = createNewCustomerTransaction(transactionRequest, accountNumber)

        val accountWithdrawalActivityRequest = AccountActivityRequest(accountNumber = accountNumber,
                transactionAmount = TransactionAmount(transactionRequest.value.amount, transactionRequest.value.currency),
                activityRemark = "",
                activityType = ActivityType.WITHDRAW)

        try {
            val accountDebitActivityResponse = accountService.doAccountActivity(accountWithdrawalActivityRequest)

            when {
                accountDebitActivityResponse.status == ActivityStatus.COMPLETED -> {
                    val accountDepositActivityRequest = AccountActivityRequest(accountNumber = transactionRequest.creditor.accountNumber,
                            transactionAmount = TransactionAmount(transactionRequest.value.amount, transactionRequest.value.currency),
                            activityRemark = "",
                            activityType = ActivityType.DEPOSIT)

                    val accountCreditActivityResponse=accountService.doAccountActivity(accountDepositActivityRequest)
                    return when {
                        accountCreditActivityResponse.status == ActivityStatus.ERROR -> {
                            reverseTransaction(accountNumber, transactionRequest)
                            updateCustomerTransaction(customerTransaction = createCustomerTransaction,value = transactionRequest.value,message = "Reverse Transaction",status = TransactionActivityStatus.FAILED)
                        }
                        else -> updateCustomerTransaction(customerTransaction = createCustomerTransaction,value = transactionRequest.value,message = "",status = TransactionActivityStatus.COMPLETED)
                    }

                }
                accountDebitActivityResponse.status == ActivityStatus.ERROR -> {
                    return updateCustomerTransaction(customerTransaction = createCustomerTransaction,value = transactionRequest.value,message = accountDebitActivityResponse.message!!,status = TransactionActivityStatus.FAILED)

                }
            }
        }catch (ex:Exception){
           return  updateCustomerTransaction(customerTransaction = createCustomerTransaction,value = transactionRequest.value,message = ex.message.toString(),status = TransactionActivityStatus.ERROR)

        }

        return TransactionResponse(id=createCustomerTransaction.id!!,status = TransactionActivityStatus.FAILED,value = transactionRequest.value)


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


        return TransactionResponse(id=customerTransaction.id!!,status = customerTransaction.status,message = message,value = value)
    }
}
