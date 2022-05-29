package ms.safi.spring.spa.servlet

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.resource.ResourceResolverChain
import javax.servlet.http.HttpServletRequest

internal class IndexFallbackResourceResolverTest {

    private lateinit var subject: IndexFallbackResourceResolver
    private lateinit var sampleRequest: HttpServletRequest
    private lateinit var locations: MutableList<Resource>
    private lateinit var mockChain: ResourceResolverChain

    @BeforeEach
    fun setUp() {
        subject = IndexFallbackResourceResolver()
        sampleRequest = MockHttpServletRequest()
        locations = mutableListOf(ClassPathResource("/"))
        mockChain = mockk()
    }

    @Test
    fun `resolveResource delegates to the resolver chain to resolve resources`() {
        val expectedResource = ClassPathResource("/found")
        every {
            mockChain.resolveResource(sampleRequest, "/hi", locations)
        } returns expectedResource

        val actual = subject.resolveResource(sampleRequest, "/hi", locations, mockChain)

        assertThat(actual).isEqualTo(expectedResource)
    }

    @Test
    fun `resolveResource resolves index html when requested resource cannot be resolved`() {
        every {
            mockChain.resolveResource(sampleRequest, "/notfound", locations)
        } returns null

        val expectedResource = ClassPathResource("/found")
        every {
            mockChain.resolveResource(sampleRequest, "/index.html", locations)
        } returns expectedResource

        val actual = subject.resolveResource(sampleRequest, "/notfound", locations, mockChain)

        assertThat(actual).isEqualTo(expectedResource)

        verifyOrder {
            mockChain.resolveResource(sampleRequest, "/notfound", locations)
            mockChain.resolveResource(sampleRequest, "/index.html", locations)
        }
    }

    @Test
    fun `resolveUrlPath delegates to the resolver chain`() {
        every {
            mockChain.resolveUrlPath("/hi", locations)
        } returns "/resolved-path"

        val actual = subject.resolveUrlPath("/hi", locations, mockChain)

        assertThat(actual).isEqualTo("/resolved-path")
    }
}