package ms.safi.spring.spa.servlet

import org.springframework.core.io.Resource
import org.springframework.web.servlet.resource.PathResourceResolver
import org.springframework.web.servlet.resource.ResourceResolverChain
import javax.servlet.http.HttpServletRequest

class IndexFallbackPathResourceResolver : PathResourceResolver() {
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
