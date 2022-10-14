package ms.safi.spring.spa.reactive

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.ViewResolverRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.resource.EncodedResourceResolver
import org.springframework.web.reactive.resource.VersionResourceResolver
import org.springframework.web.reactive.result.view.RedirectView
import org.springframework.web.reactive.result.view.UrlBasedViewResolver

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

    override fun configureViewResolvers(registry: ViewResolverRegistry) {
        val viewResolver = UrlBasedViewResolver()
//        viewResolver.setViewClass(RedirectView::class.java)
//        viewResolver.setViewNames("redirect:/index.html")
        viewResolver.order = Ordered.HIGHEST_PRECEDENCE
        registry.viewResolver(viewResolver)
        super.configureViewResolvers(registry)
    }

    @Bean
    fun indexRouterFunction(): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            GET("/").and(accept(MediaType.TEXT_HTML))
        ) {
            ServerResponse.ok().render("redirect:/index.html")
        }
    }
}
