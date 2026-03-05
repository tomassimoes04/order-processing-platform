package pt.unl.fct.iadi.orderprocessingplatform

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.orderprocessingplatform.domain.Order
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
        val receipt = paymentGateway.processPayment(PaymentRequest(order.id, roundedTotal))

        output.add("Order ID: ${order.id}")
        output.add("User ID: ${order.userId}")
        output.add("Created at: ${DateTimeFormatter.ISO_INSTANT.format(order.createdAt.atOffset(ZoneOffset.UTC))}")
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
        output.add("Payment Gateway: ${receipt.metadata["gateway"]}")

        receipt.metadata["transactionId"]?.let { output.add("Transaction ID: $it") }
        receipt.metadata["reason"]?.let { output.add("Reason: $it") }

        output.add("")
        output.add("=== Processing Complete ===")
        return output
    }

    override fun run(vararg args: String?) {
        // Updated to use the nested OrderItem and correct parameter order
        val sampleOrder = Order(
            "ORD-2026-001",
            listOf(
                Order.OrderItem("LAPTOP-001", 2, BigDecimal("999.99")),
                Order.OrderItem("MOUSE-042", 6, BigDecimal("29.99")),
                Order.OrderItem("KEYBOARD-123", 1, BigDecimal("149.99"))
            ),
            "user123"
        )

        processOrder(sampleOrder).forEach { println(it) }
    }
}