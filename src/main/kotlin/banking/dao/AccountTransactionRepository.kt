package banking.dao

import banking.model.AccountTransaction
import java.util.*
import javax.validation.constraints.NotBlank

interface AccountTransactionRepository {

    fun findById(id: UUID): AccountTransaction?

    fun findTransactionsOfAccount(@NotBlank accountNumber: Long): List<AccountTransaction>

    fun save(@NotBlank accountTransaction: AccountTransaction): AccountTransaction

}