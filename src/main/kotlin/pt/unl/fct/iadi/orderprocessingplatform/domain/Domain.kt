package pt.unl.fct.iadi.orderprocessingplatform.domain

import java.time.Instant

data class Order(
    val id: String,
    val items: List<OrderItem>,
    val userId: String,
    val createdAt: Instant = Instant.now()
) {
    data class OrderItem(
        val productId: String,
        val quantity: Int,
        val price: Double
    )
}

data class PaymentRequest(
    val orderId: String,
    val amount: Double
)

enum class ReceiptStatus {
    PAID, REJECTED, FLAGGED_FOR_REVIEW
}

data class Receipt(
    val orderId: String,
    val status: ReceiptStatus,
    val metadata: Map<String, Any>
)