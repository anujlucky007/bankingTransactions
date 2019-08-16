package odelia.micronaut.jpa.kotlin

import banking.model.Account
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.spring.tx.annotation.Transactional
import java.util.*
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.validation.constraints.NotBlank

@Singleton
open class AccountRepositoryImpl(@param:CurrentSession @field:PersistenceContext
                          private val entityManager: EntityManager,
                                 private val applicationConfiguration: ApplicationConfiguration) : AccountRepository {

    @Transactional(readOnly = true)
    override fun findById(id: Long): Account? {
        return entityManager.find(Account::class.java, id)
    }

    @Transactional
    override fun save(account: Account): Account {
        entityManager.persist(account)
        return account
    }

    @Transactional
    override fun updateBalance(id: Long, @NotBlank updatedBalance: Double): Int {

        val queryString=""
        return entityManager.createQuery(queryString)
                .setParameter("name", updatedBalance)
                .setParameter("id", id)
                .executeUpdate()
    }

    companion object {
        private val VALID_PROPERTY_NAMES = Arrays.asList("id", "name")
    }
}