package ms.safi.example.spa

import ms.safi.spring.spa.devserver.DevServerConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(DevServerConfiguration::class)
class SpringBootStarterSpaApplication

fun main(args: Array<String>) {
	runApplication<SpringBootStarterSpaApplication>(*args)
}
