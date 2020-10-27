package ms.safi.spring.spa.devserver

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = ["spa.enabled"])
class DevServerRunnerAutoConfiguration {
    @Bean
    fun devServerRunnerTask() = DevServerRunner(SimpleAsyncTaskExecutor("dev-server"))
}
