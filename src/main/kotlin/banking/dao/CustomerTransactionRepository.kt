package banking.dao

import banking.model.CustomerTransaction
import javax.validation.constraints.NotBlank

interface CustomerTransactionRepository {

    fun findById(id: Long): CustomerTransaction?

    fun save(@NotBlank customerTransaction: CustomerTransaction): CustomerTransaction

    fun updateCustomerTransaction(@NotBlank customerTransaction: CustomerTransaction): CustomerTransaction
}