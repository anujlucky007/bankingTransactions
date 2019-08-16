package banking.dao

import banking.model.Account
import javax.validation.constraints.NotBlank

interface AccountRepository {

    fun findById(id: Long): Account?

    fun save(@NotBlank account: Account): Account

    fun updateAccount(@NotBlank account: Account): Account
}