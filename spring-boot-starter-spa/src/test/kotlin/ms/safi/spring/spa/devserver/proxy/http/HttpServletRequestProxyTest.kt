package ms.safi.spring.spa.devserver.proxy.http

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

internal class HttpServletRequestProxyTest {
    private lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setup() {
        wireMockServer = WireMockServer(0)
        wireMockServer.start()
    }

    @AfterEach
    fun teardown() {
        wireMockServer.stop()
    }

    @Test
    fun `proxies request to target server`() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/some/path"))
                .withQueryParam("param", equalTo("foobar"))
                .withHeader("Accept", equalTo("text/html"))
                .withHeader("Accept", equalTo("text/plain"))
                .willReturn(
                    aResponse()
                        .withStatus(201)
                        .withHeader("Set-Cookie", "first=foo")
                        .withHeader("Set-Cookie", "second=bar")
                        .withBody("Hello world!")
                )
        )

        val proxy = HttpServletRequestProxy(targetUrl = wireMockServer.baseUrl())
        val mockResponse = MockHttpServletResponse()

        val mockRequest = MockHttpServletRequest("POST", "/some/path")
        mockRequest.queryString = "param=foobar"
        mockRequest.addHeader("Accept", "text/html")
        mockRequest.addHeader("Accept", "text/plain")

        proxy(mockRequest, mockResponse)

        assertThat(mockResponse.status).isEqualTo(201)
        assertThat(mockResponse.contentAsString).isEqualTo("Hello world!")
        assertThat(mockResponse.getHeaderValues("Set-Cookie")).containsExactlyInAnyOrder("first=foo", "second=bar")
    }

    @Test
    fun `sends no-cache for cache-control header`() {
        wireMockServer.stubFor(
            get(anyUrl())
                .withHeader("Cache-Control", equalTo("no-cache"))
                .willReturn(
                    aResponse()
                        .withBody("success")
                )
        )

        val proxy = HttpServletRequestProxy(targetUrl = wireMockServer.baseUrl())
        val mockResponse = MockHttpServletResponse()

        val mockRequest = MockHttpServletRequest("GET", "/cache")

        proxy(mockRequest, mockResponse)

        assertThat(mockResponse.contentAsString).isEqualTo("success")

    }

    @Test
    fun `removes Transfer-Encoding header received from target server`() {
        wireMockServer.stubFor(
            get(anyUrl())
                .willReturn(
                    aResponse()
                        .withHeader("Transfer-Encoding", "chunked")
                )
        )

        val proxy = HttpServletRequestProxy(targetUrl = wireMockServer.baseUrl())
        val mockResponse = MockHttpServletResponse()

        val mockRequest = MockHttpServletRequest("GET", "/te-header")

        proxy(mockRequest, mockResponse)

        assertThat(mockResponse.headerNames.map { it.toLowerCase() }).doesNotContain("transfer-encoding")
    }
}
