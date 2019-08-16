package banking.services

import javax.inject.Singleton

@Singleton
class ExchangeService {

    fun convertCurrency(fromCurrency:String, toCurrency: String, value:Double) : Double{
        return value
    }
}