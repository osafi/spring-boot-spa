package ms.safi.spring.spa.devserver.proxy

import ms.safi.spring.spa.devserver.proxy.http.DevServerProxyServletFilter
import ms.safi.spring.spa.devserver.proxy.ws.DevServerWebSocketProxy
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = ["spa.devserver.proxy.enabled"], havingValue = "true", matchIfMissing = false)
@ConditionalOnClass(WebSocketConfigurer::class)
@EnableWebSocket
@AutoConfigureBefore(WebMvcAutoConfiguration::class)
class DevServerProxyAutoConfiguration : WebSocketConfigurer {
    @Bean
    fun devServerProxyServletFilter(handlerMappings: Map<String, HandlerMapping>): DevServerProxyServletFilter {
        println("setting up http proxy")
        return DevServerProxyServletFilter(handlerMappings)
    }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        println("configuring web socket proxy")
        registry.addHandler(DevServerWebSocketProxy(), "/sockjs-node")
    }
}
