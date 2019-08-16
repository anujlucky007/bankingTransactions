/*
 * Copyright 2017 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package banking.controller


import banking.dto.AccountDTO
import banking.model.Account
import banking.services.AccountService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post

@Controller("/account")
class AccountController(val accountService: AccountService) {

    @Get("/{accountId}}")
    fun getAccount(accountId: Long): Account {
        return accountService.getAccountDetails(accountId)
    }

    @Post("/create")
    fun createAccount(account: AccountDTO): AccountDTO {

        return  accountService.createAccount(account)
    }
}