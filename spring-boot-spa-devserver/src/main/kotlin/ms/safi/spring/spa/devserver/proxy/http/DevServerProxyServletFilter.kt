package ms.safi.spring.spa.devserver.proxy.http

import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.util.ServletRequestPathUtils
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DevServerProxyServletFilter(
    private val handlerMappings: Map<String, HandlerMapping>,
    private val httpServletRequestProxy: HttpServletRequestProxy,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
            ServletRequestPathUtils.parseAndCache(request)
        }
        return handlerMappings
            .filterKeys { key -> key != "resourceHandlerMapping" && key != "welcomePageHandlerMapping" }
            .any { it.value.getHandler(request) != null }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        httpServletRequestProxy(request, response)
    }
}
