package ms.safi.spring.spa.devserver.proxy

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("spa.devserver.proxy")
data class DevServerProxyConfigurationProperties(
    val enabled: Boolean = false,
    val port: Int = 3000
)
