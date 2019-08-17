package banking.dao

import banking.model.CustomerTransaction
import java.util.*
import javax.validation.constraints.NotBlank

interface CustomerTransactionRepository {

    fun findById(id: UUID): CustomerTransaction?

    fun save(@NotBlank customerTransaction: CustomerTransaction): CustomerTransaction

    fun updateCustomerTransaction(@NotBlank customerTransaction: CustomerTransaction): CustomerTransaction
}