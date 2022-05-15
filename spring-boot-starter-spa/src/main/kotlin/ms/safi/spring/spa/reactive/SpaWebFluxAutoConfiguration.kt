package ms.safi.spring.spa.reactive

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.resource.PathResourceResolver
import org.springframework.web.reactive.resource.ResourceResolverChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit

@AutoConfigureBefore(WebFluxAutoConfiguration::class)
@ConditionalOnClass(WebFluxConfigurer::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Configuration(proxyBeanMethods = false)
class SpaWebFluxAutoConfiguration(private val webProperties: WebProperties) : WebFluxConfigurer {

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
            exchange: ServerWebExchange?,
            requestPath: String,
            locations: MutableList<out Resource>,
            chain: ResourceResolverChain
        ): Mono<Resource> {
            return super.resolveResource(exchange, requestPath, locations, chain)
                .switchIfEmpty(super.resolveResource(exchange, "/index.html", locations, chain))

        }
    }
}
