package banking.model

import banking.dto.TransactionActivityStatus
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "CustomerTransaction")
data class CustomerTransaction(@Id
                               @GeneratedValue
                               val id: Long=0,
                               val creditorAccountNumber:Long,
                               val debitorAccountNumber: Long,
                               val status: TransactionActivityStatus,
                               val message:String?=""
                               )