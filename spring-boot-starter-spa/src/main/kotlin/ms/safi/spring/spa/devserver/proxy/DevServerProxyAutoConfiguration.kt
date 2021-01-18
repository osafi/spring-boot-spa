package ms.safi.spring.spa.devserver.proxy

import ms.safi.spring.spa.devserver.proxy.http.DevServerProxyServletFilter
import ms.safi.spring.spa.devserver.proxy.ws.DevServerWebSocketProxy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
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
@EnableConfigurationProperties(DevServerProxyConfigurationProperties::class)
class DevServerProxyAutoConfiguration(private val properties: DevServerProxyConfigurationProperties) :
    WebSocketConfigurer {
    companion object {
        private val logger = LoggerFactory.getLogger(DevServerProxyAutoConfiguration::class.java)
    }

    @Bean
    fun devServerProxyServletFilter(handlerMappings: Map<String, HandlerMapping>): DevServerProxyServletFilter {
        logger.info("Dev server HTTP proxy filter registered")
        return DevServerProxyServletFilter(handlerMappings = handlerMappings, properties = properties)
    }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(DevServerWebSocketProxy(properties), "/sockjs-node")
        logger.info("Dev server WS proxy registered on '/sockjs-node'")
    }
}
