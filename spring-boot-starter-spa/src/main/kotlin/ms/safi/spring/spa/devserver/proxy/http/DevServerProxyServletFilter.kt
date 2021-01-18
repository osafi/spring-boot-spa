package ms.safi.spring.spa.devserver.proxy.http

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Order(Ordered.HIGHEST_PRECEDENCE)
class DevServerProxyServletFilter(
    private val handlerMappings: Map<String, HandlerMapping>,
    private val httpServletRequestProxy: HttpServletRequestProxy,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return handlerMappings
            .entries
            .filter { (key) -> key != "resourceHandlerMapping" && key != "welcomePageHandlerMapping" }
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
