package ms.safi.spring.spa.servlet

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.EncodedResourceResolver
import org.springframework.web.servlet.resource.PathResourceResolver
import org.springframework.web.servlet.resource.ResourceResolverChain
import org.springframework.web.servlet.resource.VersionResourceResolver
import javax.servlet.http.HttpServletRequest

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
        // possibly add ServletContextResource via addResourceLocations here

        cacheProperties.period?.seconds?.let {
            handlerRegistration.setCachePeriod(it.toInt())
        }
        handlerRegistration.setCacheControl(cacheProperties.cachecontrol.toHttpCacheControl())
        handlerRegistration.setUseLastModified(cacheProperties.isUseLastModified)

        val chainRegistration = handlerRegistration.resourceChain(chainProperties.isCache)

        val strategy = chainProperties.strategy

        if (chainProperties.isCompressed) {
            chainRegistration.addResolver(EncodedResourceResolver())
        }

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
        }

        chainRegistration.addResolver(IndexFallbackPathResourceResolver())
    }

    class IndexFallbackPathResourceResolver : PathResourceResolver() {
        override fun resolveResource(
            request: HttpServletRequest?,
            requestPath: String,
            locations: MutableList<out Resource>,
            chain: ResourceResolverChain
        ): Resource? {
            return super.resolveResource(request, requestPath, locations, chain)
                ?: super.resolveResource(request, "/index.html", locations, chain)
        }
    }
}
