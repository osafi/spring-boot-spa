package ms.safi.spring.spa.devserver

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("spa.devserver")
data class DevServerConfigurationProperties(
        val runner: RunnerProperties,
        val proxy: ProxyProperties
) {
    data class RunnerProperties(
            val enabled: Boolean = false,
            val packageManagerCommand: PackageManager = PackageManager.NPM,
            val scriptName: String = "start",
            val workingDirectory: String = "src/js"
    )
    enum class PackageManager {
        NPM, YARN
    }
    data class ProxyProperties(
            val enabled: Boolean = false
    )
}
