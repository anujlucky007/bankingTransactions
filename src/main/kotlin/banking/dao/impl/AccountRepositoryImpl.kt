package banking.dao.impl

import banking.dao.AccountRepository
import banking.model.Account
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.spring.tx.annotation.Transactional
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.validation.constraints.NotBlank

@Singleton
open class AccountRepositoryImpl(@param:CurrentSession @field:PersistenceContext
                          private val entityManager: EntityManager) : AccountRepository {

    @Transactional
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
        return entityManager.createQuery("update Account set accountBalance = :newBalance where id = :id")
                .setParameter("newBalance", updatedBalance)
                .setParameter("id", id)
                .executeUpdate()
    }
}