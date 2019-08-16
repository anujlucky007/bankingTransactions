package banking.services

import io.micronaut.context.annotation.ConfigurationProperties
import javax.inject.Singleton

@Singleton
class ExchangeService(var exchangeConfig: ExchangeConfig) {

    fun convertCurrency(fromCurrency:String, toCurrency: String, value:Double) : Double{
        var exchangeRateMap=exchangeConfig.conversionMap[fromCurrency.toLowerCase()]
        val exchangeRate=exchangeRateMap?.get(toCurrency.toLowerCase())
        return exchangeRate?.times(value)!!
    }
}

@ConfigurationProperties("application.exchangeRate")
class ExchangeConfig{

    lateinit var conversionMap:Map<String,Map<String,Double>>
}