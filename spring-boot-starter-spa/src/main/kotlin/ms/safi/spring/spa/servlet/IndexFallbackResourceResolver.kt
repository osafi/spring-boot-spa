package ms.safi.spring.spa.servlet

import org.springframework.core.io.Resource
import org.springframework.web.servlet.resource.ResourceResolver
import org.springframework.web.servlet.resource.ResourceResolverChain
import javax.servlet.http.HttpServletRequest

class IndexFallbackResourceResolver : ResourceResolver {
    override fun resolveResource(
        request: HttpServletRequest?,
        requestPath: String,
        locations: MutableList<out Resource>,
        chain: ResourceResolverChain
    ): Resource? {
        return chain.resolveResource(request, requestPath, locations)
            ?: chain.resolveResource(request, "/index.html", locations)
    }

    override fun resolveUrlPath(
        resourceUrlPath: String,
        locations: MutableList<out Resource>,
        chain: ResourceResolverChain
    ): String? {
        return chain.resolveUrlPath(resourceUrlPath, locations)
    }
}
