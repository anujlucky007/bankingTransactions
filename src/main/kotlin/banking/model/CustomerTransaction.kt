package banking.model

import banking.dto.TransactionActivityStatus
import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "CustomerTransaction")
data class CustomerTransaction(@Id
                               @GeneratedValue(generator = "UUID")
                               @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
                               val id: UUID?=null,
                               val creditorAccountNumber:Long,
                               val debitorAccountNumber: Long,
                               val status: TransactionActivityStatus,
                               val message:String?=""
                               )