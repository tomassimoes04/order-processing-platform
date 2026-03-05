package pt.unl.fct.iadi.orderprocessingplatform.payment

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.orderprocessingplatform.domain.PaymentRequest
import pt.unl.fct.iadi.orderprocessingplatform.domain.Receipt
import pt.unl.fct.iadi.orderprocessingplatform.domain.ReceiptStatus
import java.util.UUID

interface PaymentGateway {
    fun processPayment(request: PaymentRequest): Receipt
}

@Component
@Profile("!prod")
class SandboxPaymentGateway : PaymentGateway {
    override fun processPayment(request: PaymentRequest): Receipt {
        val metadata = mapOf("gateway" to "sandbox", "amount" to request.amount)
        return Receipt(request.orderId, ReceiptStatus.PAID, metadata)
    }
}

@Component
@Profile("prod")
class StripeLikePaymentGateway : PaymentGateway {
    override fun processPayment(request: PaymentRequest): Receipt {
        val amount = request.amount
        return when {
            amount <= 0.0 -> Receipt(request.orderId, ReceiptStatus.REJECTED,
                mapOf("gateway" to "stripe-like", "reason" to "Invalid amount", "amount" to amount))
            amount > 10000.0 -> Receipt(request.orderId, ReceiptStatus.FLAGGED_FOR_REVIEW,
                mapOf("gateway" to "stripe-like", "reason" to "High value transaction requires review", "amount" to amount))
            else -> Receipt(request.orderId, ReceiptStatus.PAID,
                mapOf("gateway" to "stripe-like", "transactionId" to UUID.randomUUID().toString(), "amount" to amount))
        }
    }
}