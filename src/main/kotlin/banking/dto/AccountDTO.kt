package banking.dto

import banking.model.AccountStatus
import banking.model.AccountTransaction
import banking.model.AccountType
import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class AccountDTO (val accountNumber:Long =0, val accountBalance:Double,
                       @NotEmpty
                       val customerName:String,
                       val baseCurrency:String="INR",
                       @NotNull
                       val accountType: AccountType=AccountType.SAVINGS,
                       val status: AccountStatus = AccountStatus.ACTIVE,
                       val accountTransaction :List<AccountTransactionDTO> = emptyList()
                       )

data class AccountTransactionDTO(val id: UUID,
                                 val transactionRemark:String,
                                 val amount : Double,
                                 val transactionType: ActivityType)
