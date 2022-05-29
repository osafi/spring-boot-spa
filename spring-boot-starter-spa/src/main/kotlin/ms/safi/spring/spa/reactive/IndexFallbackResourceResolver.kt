package ms.safi.spring.spa.reactive

import org.springframework.core.io.Resource
import org.springframework.web.reactive.resource.ResourceResolver
import org.springframework.web.reactive.resource.ResourceResolverChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class IndexFallbackResourceResolver : ResourceResolver {
    override fun resolveResource(
        exchange: ServerWebExchange?,
        requestPath: String,
        locations: MutableList<out Resource>,
        chain: ResourceResolverChain
    ): Mono<Resource> {
        return chain.resolveResource(exchange, requestPath, locations)
            .switchIfEmpty(Mono.defer { chain.resolveResource(exchange, "/index.html", locations) })
    }

    override fun resolveUrlPath(
        resourcePath: String,
        locations: MutableList<out Resource>,
        chain: ResourceResolverChain
    ): Mono<String> {
        return chain.resolveUrlPath(resourcePath, locations)
    }
}