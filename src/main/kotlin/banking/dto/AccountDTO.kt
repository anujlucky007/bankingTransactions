package banking.dto

import banking.model.AccountStatus
import banking.model.AccountType

data class AccountDTO (val accountNumber:Long =0, val accountBalance:Double,val accountType:AccountType,
                       val type: AccountType=AccountType.SAVINGS,
                       val status: AccountStatus = AccountStatus.ACTIVE)