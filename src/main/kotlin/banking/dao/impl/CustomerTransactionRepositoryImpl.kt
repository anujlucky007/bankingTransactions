package banking.dao.impl

import banking.dao.CustomerTransactionRepository
import banking.model.CustomerTransaction
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.spring.tx.annotation.Transactional
import java.util.*
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Singleton
open class CustomerTransactionRepositoryImpl(@param:CurrentSession @field:PersistenceContext
                          private val entityManager: EntityManager) : CustomerTransactionRepository {
    @Transactional
    override fun save(customerTransaction: CustomerTransaction): CustomerTransaction {
        entityManager.persist(customerTransaction)
        return customerTransaction
    }

    @Transactional
    override fun updateCustomerTransaction(customerTransaction: CustomerTransaction): CustomerTransaction {
        return  entityManager.merge(customerTransaction)
    }


    @Transactional(readOnly = true)
    override fun findById(id: UUID): CustomerTransaction? {
        return entityManager.find(CustomerTransaction::class.java, id)
    }


}