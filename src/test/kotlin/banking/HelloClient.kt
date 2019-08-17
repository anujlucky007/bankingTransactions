/*
 * Copyright 2018 original authors
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
package banking

import banking.dto.AccountDTO
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.reactivex.Single

import javax.validation.constraints.NotBlank

@Client("/" , errorType = Error::class)
interface HelloClient {

    @Get("/hello/{name}")
    fun hello(@NotBlank name: String): Single<String>


    @Get("/account/{accountNumber}")
    fun getAccountDetails(@NotBlank accountNumber: Long): Single<HttpResponse<Any>>

    @Post("/account/create")
    fun createAccountDetails(@NotBlank accountDto: AccountDTO): Single<AccountDTO>
}
