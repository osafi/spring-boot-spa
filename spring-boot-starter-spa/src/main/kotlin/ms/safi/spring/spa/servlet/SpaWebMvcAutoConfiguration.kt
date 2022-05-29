package ms.safi.spring.spa.servlet

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.EncodedResourceResolver
import org.springframework.web.servlet.resource.VersionResourceResolver

@ConditionalOnClass(WebMvcConfigurer::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration(proxyBeanMethods = false)
class SpaWebMvcAutoConfiguration(
    private val webMvcProperties: WebMvcProperties,
    webProperties: WebProperties,
) : WebMvcConfigurer {

    private val resourceProperties = webProperties.resources
    private val cacheProperties = resourceProperties.cache
    private val chainProperties = resourceProperties.chain

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val handlerRegistration = registry.addResourceHandler(webMvcProperties.staticPathPattern)
        handlerRegistration.addResourceLocations(*resourceProperties.staticLocations)

        cacheProperties.period?.seconds?.let {
            handlerRegistration.setCachePeriod(it.toInt())
        }
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
