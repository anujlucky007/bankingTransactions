package banking.dto

import banking.model.AccountStatus
import banking.model.AccountType
import javax.validation.constraints.NotNull

data class AccountDTO (val accountNumber:Long =0, val accountBalance:Double,
                       @NotNull
                       val customerName:String,
                       val baseCurrency:String="INR",
                       @NotNull
                       val accountType: AccountType=AccountType.SAVINGS,
                       val status: AccountStatus = AccountStatus.ACTIVE)