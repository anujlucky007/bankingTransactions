package banking.model


import banking.dto.ActivityType
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "Account")
data class Account(
        @Id
        @GeneratedValue
        val id: Long,
        val baseCurrency: String ="",
        val customerName:String,
        var accountBalance: Double=0.0,
        val lockedBalance: Double=0.0,
        val type: AccountType=AccountType.SAVINGS,
        var status: AccountStatus= AccountStatus.ACTIVE
)

enum class AccountStatus {
CLOSED,
    ACTIVE
}

enum class AccountType {
SAVINGS, CURRENT
}

@Entity
@Table(name = "AccountTransaction")
data class AccountTransaction(
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        val id: UUID?=null,
        @Column(name = "transactionRemark", nullable = false)
        val transactionRemark:String,
        @Column(name = "amount", nullable = false)
        val amount : Double,
        @Column(name = "transactionType", nullable = false)
        val transactionType: ActivityType,
        @JsonIgnore
        @ManyToOne
        val account:Account
)
