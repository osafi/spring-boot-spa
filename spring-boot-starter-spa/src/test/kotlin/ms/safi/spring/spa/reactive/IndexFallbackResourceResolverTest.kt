package ms.safi.spring.spa.reactive

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.mock.http.server.reactive.MockServerHttpRequest.get
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.resource.ResourceResolverChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration

@Suppress("ReactiveStreamsUnusedPublisher")
internal class IndexFallbackResourceResolverTest {

    private lateinit var subject: IndexFallbackResourceResolver
    private lateinit var sampleExchange: ServerWebExchange
    private lateinit var locations: MutableList<Resource>
    private lateinit var mockChain: ResourceResolverChain
    private val timeout: Duration = Duration.ofSeconds(5)


    @BeforeEach
    fun setUp() {
        subject = IndexFallbackResourceResolver()
        sampleExchange = MockServerWebExchange.from(get("/sample"))
        locations = mutableListOf(ClassPathResource("/"))
        mockChain = mockk()
    }

    @Test
    fun `resolveResource delegates to the resolver chain to resolve resources`() {
        val expectedResource = ClassPathResource("/found")
        every {
            mockChain.resolveResource(sampleExchange, "/hi", locations)
        } returns Mono.just(expectedResource)

        val actual = subject.resolveResource(sampleExchange, "/hi", locations, mockChain).block(timeout)

        assertThat(actual).isEqualTo(expectedResource)
        verify(exactly = 1) {
            mockChain.resolveResource(any(), any(), any())
        }
    }

    @Test
    fun `resolveResource resolves index html when requested resource cannot be resolved`() {
        every {
            mockChain.resolveResource(sampleExchange, "/notfound", locations)
        } returns Mono.empty()

        val expectedResource = ClassPathResource("/found")
        every {
            mockChain.resolveResource(sampleExchange, "/index.html", locations)
        } returns Mono.just(expectedResource)

        val actual = subject.resolveResource(sampleExchange, "/notfound", locations, mockChain).block(timeout)

        assertThat(actual).isEqualTo(expectedResource)

        verifyOrder {
            mockChain.resolveResource(sampleExchange, "/notfound", locations)
            mockChain.resolveResource(sampleExchange, "/index.html", locations)
        }
    }

    @Test
    fun `resolveUrlPath delegates to the resolver chain`() {
        every {
            mockChain.resolveUrlPath("/hi", locations)
        } returns Mono.just("/resolved-path")

        val actual = subject.resolveUrlPath("/hi", locations, mockChain).block(timeout)

        assertThat(actual).isEqualTo("/resolved-path")
    }
}