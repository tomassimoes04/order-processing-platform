package pt.unl.fct.iadi.orderprocessingplatform

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrderProcessingApplication

fun main(args: Array<String>) {
    runApplication<OrderProcessingApplication>(*args)
}