package ms.safi.spring.spa.devserver

import ms.safi.spring.spa.devserver.http.DevServerProxyServletFilter
import ms.safi.spring.spa.devserver.ws.DevServerWebSocketProxy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class DevServerConfiguration : WebSocketConfigurer {

    @Bean("devServerExecutor")
    fun executor() = SimpleAsyncTaskExecutor("dev-server")

    @Bean
    fun startDevServerTask() = StartDevServerTask(executor())

    @Bean
    fun devServerProxyServletFilter(handlerMappings: Map<String, HandlerMapping>) = DevServerProxyServletFilter(handlerMappings)

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(DevServerWebSocketProxy(), "/sockjs-node")
    }
}
