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

import banking.model.TransactionRequest
import banking.services.TransactionService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.validation.Validated
import javax.validation.Valid

@Controller("/transaction")
@Validated
class TransactionController(val transactionService : TransactionService) {

    @Post("/{accountNumber}/transaction-request")
    fun hello(accountNumber :String,@Body @Valid transactionRequest: TransactionRequest): String {
        return "anuj"
        //return transactionService.transact(accountNumber,transactionRequest)
    }
}