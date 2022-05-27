package ms.safi.spring.spa.servlet

import org.springframework.core.io.Resource
import org.springframework.web.servlet.resource.ResourceResolver
import org.springframework.web.servlet.resource.ResourceResolverChain
import javax.servlet.http.HttpServletRequest

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
        request: HttpServletRequest?,
        requestPath: String,
        locations: MutableList<out Resource>
    ): Resource? {
        return nextChain?.let { resolver?.resolveResource(request, requestPath, locations, nextChain) }
    }

    override fun resolveUrlPath(resourcePath: String, locations: MutableList<out Resource>): String? {
        return nextChain?.let { resolver?.resolveUrlPath(resourcePath, locations, nextChain) }
    }
}