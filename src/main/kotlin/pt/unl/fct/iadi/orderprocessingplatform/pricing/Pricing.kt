package pt.unl.fct.iadi.orderprocessingplatform.pricing

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.orderprocessingplatform.domain.Order

interface PriceCalculator {
    fun calculateTotalPrice(order: Order): Double
}

@Component
@ConditionalOnProperty(name = ["pricing.promo.enabled"], havingValue = "false", matchIfMissing = true)
class BasicPriceCalculator : PriceCalculator {
    override fun calculateTotalPrice(order: Order): Double {
        return order.items.fold(0.0) { acc, item ->
            acc + (item.price * item.quantity)
        }
    }
}

@Component
@ConditionalOnProperty(name = ["pricing.promo.enabled"], havingValue = "true")
class PromoPriceCalculator : PriceCalculator {
    override fun calculateTotalPrice(order: Order): Double {
        return order.items.fold(0.0) { acc, item ->
            val itemTotal = if (item.quantity > 5) {
                (item.price * item.quantity) * 0.8
            } else {
                item.price * item.quantity
            }
            acc + itemTotal
        }
    }
}