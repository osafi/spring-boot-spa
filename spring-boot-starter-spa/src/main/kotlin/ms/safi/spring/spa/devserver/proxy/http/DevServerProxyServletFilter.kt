package ms.safi.spring.spa.devserver.proxy.http

import ms.safi.spring.spa.devserver.proxy.DevServerProxyConfigurationProperties
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerMapping
import java.net.HttpURLConnection
import java.net.URL
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Order(Ordered.HIGHEST_PRECEDENCE)
class DevServerProxyServletFilter(
    private val handlerMappings: Map<String, HandlerMapping>,
    properties: DevServerProxyConfigurationProperties
) : OncePerRequestFilter() {
    private val devServerBaseUrl = "http://localhost:${properties.port}"

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
        forwardToWebpackDevServer(request, response)
    }

    private fun forwardToWebpackDevServer(req: HttpServletRequest, resp: HttpServletResponse) {
        val path = req.requestURI
        val queryParams = req.queryString?.let { "?$it" } ?: ""
        val url = "$devServerBaseUrl$path$queryParams"
        logger.debug("[${req.method}] $url")

        try {
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
            throw RuntimeException("Error forwarding request to dev server: [${req.method}] $url", e)
        }
    }
}
