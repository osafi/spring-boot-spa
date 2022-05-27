package ms.safi.spring.spa.servlet

import io.mockk.mockk
import io.mockk.verify
import ms.safi.spring.spa.util.files.FileBuilder
import ms.safi.spring.spa.util.files.junit.TempFileBuilder
import ms.safi.spring.spa.util.files.junit.TemporaryFileBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.util.FileCopyUtils
import org.springframework.web.servlet.resource.*
import java.nio.charset.StandardCharsets

@ExtendWith(TemporaryFileBuilder::class)
internal class IndexLinkResourceTransformerTest {

    private lateinit var transformerChain: ResourceTransformerChain
    private val indexHtmlRequest = MockHttpServletRequest("GET", "/index.html")
    private val indexHtmlResource = ClassPathResource("static/index.html")

    private lateinit var file: FileBuilder

    @BeforeEach
    fun setUp(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) {
        this.file = file

        val resolvers = listOf(
            VersionResourceResolver().apply {
                strategyMap = mutableMapOf<String, VersionStrategy>("/**" to ContentVersionStrategy())
            },
            PathResourceResolver().apply {
                setAllowedLocations(ClassPathResource("static/"))
            }
        )

        val indexLinkResourceTransformer = IndexLinkResourceTransformer().apply {
            resourceUrlProvider = ResourceUrlProvider().apply {
                setHandlerMap(mapOf(
                    "/**" to ResourceHttpRequestHandler().apply {
                        setResourceResolvers(resolvers)
                        locations = listOf(ClassPathResource("static/"))
                    }
                ))
            }
        }

        transformerChain = TestResourceTransformerChain(
            resolverChain = TestResourceResolverChain(resolvers),
            transformers = listOf(indexLinkResourceTransformer)
        )

        file("static/js/main.js", "sample js file")
        file("static/css/main.css", "sample css file")
    }

    @Test
    fun `transforms href and src links in index html to versioned link`() {
        file(
            "static/index.html", """
            <html>
            <head>
                <script defer="defer" src="/js/main.js"></script>
                <script defer="defer" src = "/js/main.js"></script>
                <script defer="defer" src='/js/main.js'></script>
                <link href="/css/main.css" rel="stylesheet">
                <link href='/css/main.css' rel="stylesheet">
                <link key="/css/main.css">
            </head>
            <body>
                <div id="root"></div>
            </body>
            </html>
        """.trimIndent()
        )

        val result = transformerChain.transform(indexHtmlRequest, indexHtmlResource).asString()

        assertThat(result).isEqualTo(
            """
            <html>
            <head>
                <script defer="defer" src="/js/main-cc5982c68429067dcccddfd415354d46.js"></script>
                <script defer="defer" src = "/js/main-cc5982c68429067dcccddfd415354d46.js"></script>
                <script defer="defer" src='/js/main-cc5982c68429067dcccddfd415354d46.js'></script>
                <link href="/css/main-ba001f03e70ae781348d41921b77fc5b.css" rel="stylesheet">
                <link href='/css/main-ba001f03e70ae781348d41921b77fc5b.css' rel="stylesheet">
                <link key="/css/main.css">
            </head>
            <body>
                <div id="root"></div>
            </body>
            </html>
        """.trimIndent()
        )
    }

    @Test
    fun `does not transform external links`() {
        val fileContent = """
            <html>
            <head>
                <script defer="defer" src="https://example.com/js/main.js"></script>
                <script defer="defer" src="file:///home/js/main.js"></script>
                <script defer="defer" src="//example.com/js/main.js"></script>
            </head>
            <body>
                <div id="root"></div>
            </body>
            </html>
        """.trimIndent()
        file("static/index.html", fileContent)

        val mockResolverChain = mockk<ResourceResolverChain>()
        val transformerChain = TestResourceTransformerChain(mockResolverChain, listOf(IndexLinkResourceTransformer()))

        val result = transformerChain.transform(indexHtmlRequest, indexHtmlResource).asString()

        assertThat(result).isEqualTo(fileContent)
        verify(exactly = 0) { mockResolverChain.resolveUrlPath(any(), any()) }
    }

    @Test
    fun `only transforms index html file`() {
        val fileContent = """
            <html>
            <head>
                <script defer="defer" src="/js/main.js"></script>
            </head>
            </html>
        """.trimIndent()
        file("static/other.html", fileContent)

        val result = transformerChain.transform(
            MockHttpServletRequest("GET", "/other.html"),
            ClassPathResource("static/other.html")
        ).asString()

        assertThat(result).isEqualTo(fileContent)
    }

    private fun Resource.asString(): String {
        return String(FileCopyUtils.copyToByteArray(this.inputStream), StandardCharsets.UTF_8)
    }
}