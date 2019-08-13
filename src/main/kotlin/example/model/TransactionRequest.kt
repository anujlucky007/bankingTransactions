package banking.model

import java.io.Serializable


data class TransactionRequest (val requestId : String, val description : String, val creditor: Creditor, val value: Value) : Serializable

data class Creditor(
        val counterparty_id: String
) : Serializable
data class Value(
        var amount: String,
        val currency: String
) : Serializable

data class Account(val id:String, val baseCurrency:String, val accountBalance : Double,val lockedBalance:Double,val transactionList :List<String>)