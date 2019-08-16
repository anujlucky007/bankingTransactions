package odelia.micronaut.jpa.kotlin

import banking.model.Account
import javax.validation.constraints.NotBlank

interface AccountRepository {

    fun findById(id: Long): Account?

    fun save(@NotBlank account: Account): Account

    fun updateBalance(id: Long, @NotBlank updatedBalance: Double): Int
}