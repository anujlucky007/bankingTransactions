package banking.dao

import banking.model.AccountTransaction
import javax.validation.constraints.NotBlank

interface AccountTransactionRepository {

    fun findById(id: Long): AccountTransaction?

    fun findTransactionsOfAccount(@NotBlank accountNumber: Long): List<AccountTransaction>

    fun save(@NotBlank accountTransaction: AccountTransaction): AccountTransaction

}