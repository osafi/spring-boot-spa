package ms.safi.example.spa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringReactiveSpaApplication

fun main(args: Array<String>) {
	runApplication<SpringReactiveSpaApplication>(*args)
}
