package ms.safi.spring.spa.reactive

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.resource.EncodedResourceResolver
import org.springframework.web.reactive.resource.VersionResourceResolver

@ConditionalOnClass(WebFluxConfigurer::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Configuration(proxyBeanMethods = false)
class SpaWebFluxAutoConfiguration(
    private val webFluxProperties: WebFluxProperties,
    webProperties: WebProperties,
) : WebFluxConfigurer {

    private val resourceProperties = webProperties.resources
    private val cacheProperties = resourceProperties.cache
    private val chainProperties = resourceProperties.chain

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val handlerRegistration = registry.addResourceHandler(webFluxProperties.staticPathPattern)
        handlerRegistration.addResourceLocations(*resourceProperties.staticLocations)

        handlerRegistration.setCacheControl(cacheProperties.cachecontrol.toHttpCacheControl())
        handlerRegistration.setUseLastModified(cacheProperties.isUseLastModified)

        val chainRegistration = handlerRegistration.resourceChain(chainProperties.isCache)
        chainRegistration.addResolver(IndexFallbackResourceResolver())

        if (chainProperties.isCompressed) {
            chainRegistration.addResolver(EncodedResourceResolver())
        }

        val strategy = chainProperties.strategy
        if (strategy.fixed.isEnabled || strategy.content.isEnabled) {
            val resolver = VersionResourceResolver()
            if (strategy.fixed.isEnabled) {
                val version = strategy.fixed.version
                val paths = strategy.fixed.paths
                resolver.addFixedVersionStrategy(version, *paths)
            }
            if (strategy.content.isEnabled) {
                val paths = strategy.content.paths
                resolver.addContentVersionStrategy(*paths)
            }
            chainRegistration.addResolver(resolver)
            chainRegistration.addTransformer(IndexLinkResourceTransformer())
        }
    }
}
