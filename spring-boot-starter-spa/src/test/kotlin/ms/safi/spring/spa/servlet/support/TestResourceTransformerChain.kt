package ms.safi.spring.spa.servlet.support

import org.springframework.core.io.Resource
import org.springframework.web.servlet.resource.ResourceResolverChain
import org.springframework.web.servlet.resource.ResourceTransformer
import org.springframework.web.servlet.resource.ResourceTransformerChain
import javax.servlet.http.HttpServletRequest

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

    override fun transform(request: HttpServletRequest, resource: Resource): Resource {
        return nextChain?.let { transformer?.transform(request, resource, nextChain) }
            ?: resource
    }
}