package odelia.micronaut.jpa.kotlin

import banking.model.AccountTransaction
import javax.validation.constraints.NotBlank

interface AccountTransactionRepository {

    fun findById(id: Long): AccountTransaction?

    fun save(@NotBlank accountTransaction: AccountTransaction): AccountTransaction

}