package banking.dto

import java.io.Serializable
import java.util.*


data class TransactionRequest (val requestId : String, val description : String, val creditor: Creditor, val value: Value) : Serializable

data class Creditor(
        val bank_id: String,
        val accountNumber : Long
) : Serializable
data class Value(
        var amount: Double,
        val currency: String
) : Serializable

data class TransactionResponse (val id : UUID, val value: Value, val status : TransactionActivityStatus,val message:String?="") : Serializable

enum class TransactionActivityStatus {
    COMPLETED,
    INPROGRESS,
    SUBMITTED,
    FAILED,
    ERROR
}

