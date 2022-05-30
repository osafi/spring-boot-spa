package ms.safi.spring.spa.reactive.support

import org.springframework.core.io.Resource
import org.springframework.web.reactive.resource.ResourceResolverChain
import org.springframework.web.reactive.resource.ResourceTransformer
import org.springframework.web.reactive.resource.ResourceTransformerChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

internal class TestResourceTransformerChain private constructor(
    private val resolverChain: ResourceResolverChain,
    private val transformer: ResourceTransformer? = null,
    private val nextChain: ResourceTransformerChain? = null,
) : ResourceTransformerChain {

    companion object {
        operator fun invoke(
            resolverChain: ResourceResolverChain,
            transformers: List<ResourceTransformer>
        ): TestResourceTransformerChain {
            return transformers.foldRight(TestResourceTransformerChain(resolverChain)) { value, prevChain ->
                TestResourceTransformerChain(resolverChain, value, prevChain)
            }
        }
    }

    override fun getResolverChain(): ResourceResolverChain {
        return resolverChain
    }

    override fun transform(exchange: ServerWebExchange, resource: Resource): Mono<Resource> {
        return nextChain?.let { transformer?.transform(exchange, resource, nextChain) }
            ?: Mono.just(resource)
    }
}