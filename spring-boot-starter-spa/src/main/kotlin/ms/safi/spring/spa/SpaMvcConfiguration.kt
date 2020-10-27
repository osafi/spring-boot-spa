package ms.safi.spring.spa

import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import org.springframework.web.servlet.resource.ResourceResolverChain
import javax.servlet.http.HttpServletRequest

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value = [ResourceProperties::class])
class SpaMvcConfiguration(val resourceProperties: ResourceProperties) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(*resourceProperties.staticLocations)
                .resourceChain(resourceProperties.chain.isCache)
                .addResolver(FallbackPathResourceResolver())
    }

    private class FallbackPathResourceResolver : PathResourceResolver() {
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
