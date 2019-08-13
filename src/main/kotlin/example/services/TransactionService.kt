package banking.services

import banking.model.TransactionRequest
import example.services.RequestValidationService
import javax.inject.Singleton


@Singleton
class TransactionService(var requestValidationService: RequestValidationService) {

    fun transact(accountNumber: Int,transactionRequest: TransactionRequest): String {
        // 1. is transaction request valid
        requestValidationService.validateRequest(accountNumber, transactionRequest.requestId)
        //2. generate transaction id
        //3. save transaction in db (id, txn creditor remark, txn debitor remark, creditor account id , amount , currency, status )
        //4. validate account balance with curency into consideration
        //5. debit from account
        //6 credit to account -- should return some object not exception
        //send response back with transaction id generated
        //transactional property with debit and credit




        return "Hello $accountNumber"
    }
}
