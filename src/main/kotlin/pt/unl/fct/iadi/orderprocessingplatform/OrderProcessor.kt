package pt.unl.fct.iadi.orderprocessingplatform

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.orderprocessingplatform.domain.Order
import pt.unl.fct.iadi.orderprocessingplatform.domain.PaymentRequest
import pt.unl.fct.iadi.orderprocessingplatform.payment.PaymentGateway
import pt.unl.fct.iadi.orderprocessingplatform.pricing.PriceCalculator
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.round

@Component
class OrderProcessor(
    private val priceCalculator: PriceCalculator,
    private val paymentGateway: PaymentGateway
) : CommandLineRunner {

    fun processOrder(order: Order): List<String> {
        val rawTotal = priceCalculator.calculateTotalPrice(order)
        val roundedTotal = round(rawTotal * 100) / 100.0

        val receipt = paymentGateway.processPayment(PaymentRequest(order.id, roundedTotal))
        val output = mutableListOf<String>()

        output.add("Order ID: ${order.id}")
        output.add("User ID: ${order.userId}")
        output.add("Created at: ${DateTimeFormatter.ISO_INSTANT.format(order.createdAt.atOffset(ZoneOffset.UTC))}")
        output.add("")
        output.add("Items:")

        order.items.forEach { item ->
            val itemTotal = item.price * item.quantity
            // Using String.format to ensure 2 decimal places in output
            output.add(String.format("  - %s: %d x $%.2f = $%.2f", item.productId, item.quantity, item.price, itemTotal))
        }

        output.add("")
        output.add(String.format("Total Price: $%.2f", roundedTotal))
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
        val sampleOrder = Order(
            "ORD-2026-001",
            listOf(
                Order.OrderItem("LAPTOP-001", 2, 999.99),
                Order.OrderItem("MOUSE-042", 6, 29.99),
                Order.OrderItem("KEYBOARD-123", 1, 149.99)
            ),
            "user123"
        )

        processOrder(sampleOrder).forEach { println(it) }
    }
}