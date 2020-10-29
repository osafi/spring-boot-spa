package ms.safi.spring.spa.devserver

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = ["spa.devserver.runner.enabled"], havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(DevServerConfigurationProperties::class)
class DevServerRunnerAutoConfiguration {
    @Bean
    fun devServerRunnerTask(properties: DevServerConfigurationProperties) =
            DevServerRunner(executor = SimpleAsyncTaskExecutor("dev-server"), properties = properties.runner)
}
