package banking.dao.impl

import banking.dto.TransactionActivityStatus
import banking.model.CustomerTransaction
import io.kotlintest.shouldBe
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class CustomerTransactionRepositoryImplTest{

    @Inject
    lateinit var customerTransactionRepositoryImpl: CustomerTransactionRepositoryImpl



    @Test
    fun `save customer transaction in DB`(){

        val customerTransaction= CustomerTransaction(status = TransactionActivityStatus.COMPLETED,creditorAccountNumber = 1234,
                debitorAccountNumber = 4567)

       val savedCustomerTransaction=customerTransactionRepositoryImpl.save(customerTransaction)

        val actualCustomerTransaction=customerTransactionRepositoryImpl.findById(savedCustomerTransaction.id!!)

        actualCustomerTransaction!!.id shouldBe savedCustomerTransaction.id
        actualCustomerTransaction.creditorAccountNumber shouldBe 1234
        actualCustomerTransaction.debitorAccountNumber shouldBe 4567
        actualCustomerTransaction.status shouldBe TransactionActivityStatus.COMPLETED

    }

    @Test
    fun `update customer transaction in DB`(){

        val customerTransaction= CustomerTransaction(status = TransactionActivityStatus.COMPLETED,creditorAccountNumber = 1234,
                debitorAccountNumber = 4567)

        val savedCustomerTransaction=customerTransactionRepositoryImpl.save(customerTransaction)
        val changedCustomerTransaction=savedCustomerTransaction.copy(status = TransactionActivityStatus.INPROGRESS)

        customerTransactionRepositoryImpl.updateCustomerTransaction(changedCustomerTransaction)


        val actualCustomerTransaction=customerTransactionRepositoryImpl.findById(savedCustomerTransaction.id!!)

        actualCustomerTransaction!!.id shouldBe savedCustomerTransaction.id
        actualCustomerTransaction.creditorAccountNumber shouldBe 1234
        actualCustomerTransaction.debitorAccountNumber shouldBe 4567
        actualCustomerTransaction.status shouldBe TransactionActivityStatus.INPROGRESS

    }

}