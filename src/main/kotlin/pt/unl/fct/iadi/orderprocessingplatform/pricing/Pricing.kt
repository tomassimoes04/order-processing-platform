package pt.unl.fct.iadi.orderprocessingplatform.pricing

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.orderprocessingplatform.domain.Order
import java.math.BigDecimal

interface PriceCalculator {
    fun calculateTotalPrice(order: Order): BigDecimal
}


@Component
@ConditionalOnProperty(
    name = ["pricing.promo.enabled"],
    havingValue = "false",
    matchIfMissing = true
)
class BasicPriceCalculator : PriceCalculator {
    override fun calculateTotalPrice(order: Order): BigDecimal {
        return order.items.fold(BigDecimal.ZERO) { acc, item ->
            val itemTotal = item.price.multiply(BigDecimal(item.quantity))
            acc.add(itemTotal)
        }
    }
}

@Component
@ConditionalOnProperty(
    name = ["pricing.promo.enabled"],
    havingValue = "true"
)
class PromoPriceCalculator : PriceCalculator {
    override fun calculateTotalPrice(order: Order): BigDecimal {
        return order.items.fold(BigDecimal.ZERO) { acc, item ->
            val itemTotal = if (item.quantity > 5) {
                item.price.multiply(BigDecimal(item.quantity)).multiply(BigDecimal("0.8"))
            } else {
                item.price.multiply(BigDecimal(item.quantity))
            }
            acc.add(itemTotal)
        }
    }
}