package ms.safi.spring.spa.devserver.http

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
import java.net.HttpURLConnection
import java.net.URL
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Order(Ordered.HIGHEST_PRECEDENCE)
class DevServerProxyServletFilter(
        private val handlerMappings: Map<String, HandlerMapping>
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (shouldBeHandledBySpring(request)) {
            filterChain.doFilter(request, response)
        } else {
            forwardToWebpackDevServer(request, response)
        }
    }

    private fun shouldBeHandledBySpring(request: HttpServletRequest): Boolean {
        val (handlerMappingName, handlerMapping) = handlerMappings.entries.find { it.value.getHandler(request) != null }
                ?: return false

        val isResourceHandlerMapping =
                handlerMapping is SimpleUrlHandlerMapping && handlerMappingName == "resourceHandlerMapping"

        return !isResourceHandlerMapping
    }
}

fun forwardToWebpackDevServer(req: HttpServletRequest, resp: HttpServletResponse) {
    try {
        val baseUrl = "http://localhost:3000"
        val path = req.requestURI
        val queryParams = req.queryString?.let { "?$it" } ?: ""
        val url = "$baseUrl$path$queryParams"
        println("[${req.method}] $url")
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = req.method

        req.headerNames.asSequence().forEach { headerName ->
            req.getHeaders(headerName).asSequence().forEach { headerValue ->
                conn.addRequestProperty(headerName, headerValue)
            }
        }

        conn.useCaches = false
        conn.doInput = true
        conn.doOutput = false
        conn.connect()

        resp.status = conn.responseCode

        conn.headerFields
                .filterKeys { it != null && it != "Transfer-Encoding" }
                .forEach { (headerName, headerValues) ->
                    headerValues.forEach { headerValue ->
                        resp.setHeader(headerName, headerValue)
                    }
                }

        conn.inputStream.copyTo(resp.outputStream)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
