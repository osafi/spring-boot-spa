package ms.safi.spring.spa.reactive.support

import org.springframework.core.io.Resource
import org.springframework.web.reactive.resource.ResourceResolver
import org.springframework.web.reactive.resource.ResourceResolverChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

internal class TestResourceResolverChain private constructor(
    private val resolver: ResourceResolver? = null,
    private val nextChain: ResourceResolverChain? = null
) : ResourceResolverChain {

    companion object {
        operator fun invoke(resolvers: List<ResourceResolver> = emptyList()): TestResourceResolverChain {
            return resolvers.foldRight(TestResourceResolverChain()) { value, prevChain ->
                TestResourceResolverChain(value, prevChain)
            }
        }
    }

    override fun resolveResource(
        exchange: ServerWebExchange?,
        requestPath: String,
        locations: MutableList<out Resource>
    ): Mono<Resource> {
        return nextChain?.let { resolver?.resolveResource(exchange, requestPath, locations, nextChain) }
            ?: Mono.empty()
    }

    override fun resolveUrlPath(resourcePath: String, locations: MutableList<out Resource>): Mono<String> {
        return nextChain?.let { resolver?.resolveUrlPath(resourcePath, locations, nextChain) }
            ?: Mono.empty()
    }

}