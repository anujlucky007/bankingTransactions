package banking.dao

import banking.model.AccountTransaction
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.spring.tx.annotation.Transactional
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Singleton
open class AccountTransactionRepositoryImpl(@param:CurrentSession @field:PersistenceContext
                          private val entityManager: EntityManager,
                                            private val applicationConfiguration: ApplicationConfiguration) : AccountTransactionRepository {

    @Transactional(readOnly = true)
    override fun findById(id: Long): AccountTransaction? {
        return entityManager.find(AccountTransaction::class.java, id)
    }

    @Transactional
    override fun save(accountTransaction: AccountTransaction): AccountTransaction {
        entityManager.persist(accountTransaction)
        return accountTransaction
    }

}