package ms.safi.spring.spa.servlet

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import org.springframework.web.servlet.resource.ResourceResolverChain
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

@AutoConfigureBefore(WebMvcAutoConfiguration::class)
@ConditionalOnClass(WebMvcConfigurer::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration(proxyBeanMethods = false)
class SpaWebMvcConfigurer(private val webProperties: WebProperties) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .addResourceLocations("classpath:static/static/")
            .resourceChain(webProperties.resources.chain.isCache)
            .addResolver(PathResourceResolver())

        registry.addResourceHandler("/**")
            .addResourceLocations(*webProperties.resources.staticLocations)
            .resourceChain(webProperties.resources.chain.isCache)
            .addResolver(IndexFallbackPathResourceResolver())
    }

    private class IndexFallbackPathResourceResolver : PathResourceResolver() {
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
