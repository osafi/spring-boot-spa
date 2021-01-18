package ms.safi.spring.spa.devserver.proxy.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpServletRequestProxy(private val targetUrl: String) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(HttpServletRequestProxy::class.java)
    }

    fun proxyRequest(req: HttpServletRequest, resp: HttpServletResponse) {
        val path = req.requestURI
        val queryParams = req.queryString?.let { "?$it" } ?: ""
        val url = "$targetUrl$path$queryParams"
        logger.debug("[${req.method}] $path -> $url")

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
