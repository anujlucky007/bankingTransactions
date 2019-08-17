package banking.services

import banking.dto.AccountActivityRequest
import banking.dto.ActivityStatus
import banking.dto.ActivityType
import banking.dto.TransactionAmount
import banking.dto.TransactionRequest
import javax.inject.Singleton


@Singleton
class TransactionService(var requestValidationService: RequestValidationService,val accountService: AccountServiceImpl) {

    fun transact(accountNumber: Long,transactionRequest: TransactionRequest): String {
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


        requestValidationService.validateRequest(accountNumber, transactionRequest.requestId)

        val accountWithdrawalActivityRequest = AccountActivityRequest(accountNumber = accountNumber,
                transactionAmount = TransactionAmount(transactionRequest.value.amount, transactionRequest.value.currency),
                activityRemark = "",
                activityType = ActivityType.WITHDRAW)

        val accountActivityResponse=accountService.doAccountActivity(accountWithdrawalActivityRequest)

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

        return "Hello $accountNumber"
    }
}
