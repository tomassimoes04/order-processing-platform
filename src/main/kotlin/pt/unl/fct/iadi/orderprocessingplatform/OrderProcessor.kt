package pt.unl.fct.iadi.orderprocessingplatform

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.orderprocessingplatform.domain.Order
import pt.unl.fct.iadi.orderprocessingplatform.domain.OrderItem
import pt.unl.fct.iadi.orderprocessingplatform.domain.PaymentRequest
import pt.unl.fct.iadi.orderprocessingplatform.payment.PaymentGateway
import pt.unl.fct.iadi.orderprocessingplatform.pricing.PriceCalculator
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class OrderProcessor(
    private val priceCalculator: PriceCalculator,
    private val paymentGateway: PaymentGateway
) : CommandLineRunner {

    fun processOrder(order: Order): List<String> {
        val output = mutableListOf<String>()

        val rawTotal = priceCalculator.calculateTotalPrice(order)
        val roundedTotal = rawTotal.setScale(2, RoundingMode.HALF_UP)

        val paymentRequest = PaymentRequest(order.id, roundedTotal)
        val receipt = paymentGateway.processPayment(paymentRequest)

        output.add("Order ID: ${order.id}")
        output.add("User ID: ${order.userId}")

        val timeString = DateTimeFormatter.ISO_INSTANT.format(order.createdAt.atOffset(ZoneOffset.UTC))
        output.add("Created at: $timeString")
        output.add("")
        output.add("Items:")

        order.items.forEach { item ->
            val itemTotal = (item.price * BigDecimal(item.quantity)).setScale(2, RoundingMode.HALF_UP)
            output.add("  - ${item.productId}: ${item.quantity} x $${item.price} = $$itemTotal")
        }

        output.add("")
        output.add("Total Price: $$roundedTotal")
        output.add("Calculator Used: ${priceCalculator::class.simpleName}")
        output.add("")
        output.add("Payment Status: ${receipt.status}")

        val metadata = receipt.metadata
        output.add("Payment Gateway: ${metadata["gateway"]}")

        if (metadata.containsKey("transactionId")) {
            output.add("Transaction ID: ${metadata["transactionId"]}")
        }
        if (metadata.containsKey("reason")) {
            output.add("Reason: ${metadata["reason"]}")
        }

        output.add("")
        output.add("=== Processing Complete ===")

        return output
    }

    override fun run(vararg args: String?) {
        val sampleOrder = Order(
            id = "ORD-2026-001",
            userId = "user123",
            items = listOf(
                OrderItem("LAPTOP-001", 2, BigDecimal("999.99")),
                OrderItem("MOUSE-042", 6, BigDecimal("29.99")), // Quantity > 5 to trigger promo if enabled
                OrderItem("KEYBOARD-123", 1, BigDecimal("149.99"))
            )
        )

        val result = processOrder(sampleOrder)
        result.forEach { println(it) }
    }
}