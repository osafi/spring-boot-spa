package ms.safi.spring.spa

import ms.safi.spring.spa.util.files.FileBuilder
import ms.safi.spring.spa.util.files.junit.TempFileBuilder
import ms.safi.spring.spa.util.files.junit.TemporaryFileBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.CacheControl
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@ExtendWith(TemporaryFileBuilder::class)
@TestPropertySource(
    properties = [
        "spring.web.resources.chain.cache=false", // Disabling resource chain caching to prevent pollution between tests
        "spring.web.resources.chain.strategy.content.enabled=true",
        "spring.web.resources.cache.period=365d",
        "spring.web.resources.cache.use-last-modified=true"
    ]
)
abstract class SpaIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `returns index html for unknown paths`(@TempFileBuilder file: FileBuilder) {
        file("static/index.html", """I'm a sample index.html""")

        webTestClient
            .get()
            .uri("/unknown")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<String>()
            .isEqualTo("""I'm a sample index.html""")

        webTestClient
            .get()
            .uri("/api/unknown")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<String>()
            .isEqualTo("""I'm a sample index.html""")

    }

    @Test
    fun `returns static resources other than index html`(@TempFileBuilder file: FileBuilder) {
        file("static/example.txt", """I'm a sample text file""")
        file("static/static/another.txt", """I'm another text file""")

        webTestClient
            .get()
            .uri("/example.txt")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<String>()
            .isEqualTo("""I'm a sample text file""")

        webTestClient
            .get()
            .uri("/static/another.txt")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<String>()
            .isEqualTo("""I'm another text file""")
    }

    @Test
    fun `REST endpoints remain accessible`(@TempFileBuilder file: FileBuilder) {
        file("test", "file that should not be returned")

        webTestClient
            .get()
            .uri("/test")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json("""{"foo": "bar"}""")
    }

    @Test
    fun `transforms URLs in index html based on enabled cache strategy`(@TempFileBuilder file: FileBuilder) {
        file(
            "static/index.html", """
                <html>
                <head>
                    <script defer="defer" src="/js/main.js"></script>
                    <link href="/css/main.css" rel="stylesheet">
                </head>
                <body>
                    <div id="root"></div>
                </body>
                </html>
            """.trimIndent()
        )
        file("static/js/main.js", """I'm a sample JS file""")

        val expectedIndexHtml = """
            <html>
            <head>
                <script defer="defer" src="/js/main-82a9c788e1e4ac44f388a8a04a3dbb42.js"></script>
                <link href="/css/main.css" rel="stylesheet">
            </head>
            <body>
                <div id="root"></div>
            </body>
            </html>
        """.trimIndent()

        webTestClient
            .get()
            .uri("/index.html")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<String>()
            .isEqualTo(expectedIndexHtml)
    }

    @Test
    fun `includes cache-control headers`(@TempFileBuilder file: FileBuilder) {
        val mainJs = file("static/js/main.js", """I'm a sample JS file""")

        webTestClient
            .get()
            .uri("/js/main.js")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .expectHeader()
            .lastModified(Instant.ofEpochMilli(mainJs.lastModified()).truncatedTo(ChronoUnit.SECONDS).toEpochMilli())
    }
}