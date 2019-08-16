package banking.dto

data class AccountActivityRequest(internal val accountNumber: Long, val transactionAmount : TransactionAmount, val activityRemark : String, val activityType : ActivityType)

data class AccountActivityResponse(val accountNumber: Long,
                                   val updatedAccountBalance:Double,
                                   val status: ActivityStatus,
                                   val message:String?)

enum class ActivityStatus {
    COMPLETED,
    ERROR,
}


enum class ActivityType {
    WITHDRAW,
    DEPOSIT
}

data class TransactionAmount(val value:Double, val currency: String)
