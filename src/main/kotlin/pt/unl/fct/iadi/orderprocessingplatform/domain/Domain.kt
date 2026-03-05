package pt.unl.fct.iadi.orderprocessingplatform.domain

import java.math.BigDecimal
import java.time.Instant

data class OrderItem(
    val productId: String,
    val quantity: Int,
    val price: BigDecimal
)

data class Order(
    val id: String,
    val userId: String,
    val items: List<OrderItem>,
    val createdAt: Instant = Instant.now()
)

data class PaymentRequest(
    val orderId: String,
    val amount: BigDecimal
)

enum class ReceiptStatus {
    PAID, REJECTED, FLAGGED_FOR_REVIEW
}

data class Receipt(
    val orderId: String,
    val status: ReceiptStatus,
    val metadata: Map<String, Any>
)