package banking.services

import banking.GenericException
import io.micronaut.context.annotation.ConfigurationProperties
import javax.inject.Singleton

@Singleton
class ExchangeService(var exchangeConfig: ExchangeConfig) {

    fun convertCurrency(fromCurrency:String, toCurrency: String, value:Double) : Double{
        var exchangeRateMap=exchangeConfig.conversionMap[fromCurrency.toLowerCase()] ?: throw GenericException("Currency not supported","ACC.CURRENCY.INVALID.001")
        val exchangeRate=exchangeRateMap?.get(toCurrency.toLowerCase()) ?: throw GenericException("Currency not supported","ACC.CURRENCY.INVALID.001")
        return exchangeRate.times(value)
    }
}

@ConfigurationProperties("application.exchangeRate")
class ExchangeConfig{

    lateinit var conversionMap:Map<String,Map<String,Double>>
}