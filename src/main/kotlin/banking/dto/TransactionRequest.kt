package banking.model

import java.io.Serializable


data class TransactionRequest (val requestId : String, val description : String, val creditor: Creditor, val value: Value) : Serializable

data class Creditor(
        val bank_id: String,
        val accountNumber : Long
) : Serializable
data class Value(
        var amount: Double,
        val currency: String
) : Serializable