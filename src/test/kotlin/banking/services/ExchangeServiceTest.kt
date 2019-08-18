package banking.services

import banking.GenericException
import banking.dto.Creditor
import banking.dto.TransactionActivityStatus
import banking.dto.TransactionRequest
import banking.dto.Value
import banking.model.Account
import banking.model.AccountStatus
import banking.model.AccountType
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class ExchangeServiceTest{

    @Inject
    lateinit var exchangeService: ExchangeService


    @Test
    fun `should get value of amount after converting from USD to INR`(){

        val exchangedValue=exchangeService.convertCurrency("USD","INR",1.0)

        exchangedValue shouldBe 70.0

    }


    @Test
    fun `should throw error if From Currency is not supported`(){

        val exception= assertThrows(GenericException::class.java){ exchangeService.convertCurrency("UNKNOWN","INR",1.0)}

        exception.errorMessage shouldBe "Currency not supported"
        exception.errorCode shouldBe "ACC.CURRENCY.INVALID.001"

    }


    @Test
    fun `should throw error if To Currency is not supported`(){

        val exception= assertThrows(GenericException::class.java){ exchangeService.convertCurrency("INR","UNKNOWN",1.0)}

        exception.errorMessage shouldBe "Currency not supported"
        exception.errorCode shouldBe "ACC.CURRENCY.INVALID.001"

    }


}