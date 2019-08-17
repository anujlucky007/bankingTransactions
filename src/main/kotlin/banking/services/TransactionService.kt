package banking.services

import banking.dao.CustomerTransactionRepository
import banking.dto.*
import banking.model.CustomerTransaction
import java.lang.Exception
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

    fun transactIntraBank(accountNumber: Long, transactionRequest: TransactionRequest): TransactionResponse {

        requestValidationService.validateRequest(accountNumber, transactionRequest.requestId)

        var createCustomerTransaction=CustomerTransaction(creditorAccountNumber = transactionRequest.creditor.accountNumber,
                debitorAccountNumber = accountNumber,status = TransactionActivityStatus.INPROGRESS)

        createCustomerTransaction=customerTransactionRepository.save(createCustomerTransaction)

        val accountWithdrawalActivityRequest = AccountActivityRequest(accountNumber = accountNumber,
                transactionAmount = TransactionAmount(transactionRequest.value.amount, transactionRequest.value.currency),
                activityRemark = "",
                activityType = ActivityType.WITHDRAW)

        try {
            val accountActivityResponse = accountService.doAccountActivity(accountWithdrawalActivityRequest)

            when {
                accountActivityResponse.status == ActivityStatus.COMPLETED -> {
                    val accountDepositActivityRequest = AccountActivityRequest(accountNumber = transactionRequest.creditor.accountNumber,
                            transactionAmount = TransactionAmount(transactionRequest.value.amount, transactionRequest.value.currency),
                            activityRemark = "",
                            activityType = ActivityType.DEPOSIT)

                    accountService.doAccountActivity(
                            accountDepositActivityRequest)

                }
                accountActivityResponse.status == ActivityStatus.ERROR -> {

                }
            }
        }catch (ex:Exception){
            return TransactionResponse(id=createCustomerTransaction.id,status = TransactionActivityStatus.ERROR,message = ex.message.toString(),value = transactionRequest.value)
        }

        return TransactionResponse(id=createCustomerTransaction.id,status = TransactionActivityStatus.COMPLETED,value = transactionRequest.value)

    }
}
