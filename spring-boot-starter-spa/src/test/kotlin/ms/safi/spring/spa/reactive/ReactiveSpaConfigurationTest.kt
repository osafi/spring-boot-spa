package ms.safi.spring.spa.reactive

import ms.safi.spring.spa.TestRestController
import ms.safi.spring.spa.util.files.FileBuilder
import ms.safi.spring.spa.util.files.junit.TempFileBuilder
import ms.safi.spring.spa.util.files.junit.TemporaryFileBuilder
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.CacheControl
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.concurrent.TimeUnit

@ExtendWith(TemporaryFileBuilder::class)
@Import(ReactiveSpaConfiguration::class)
@WebFluxTest(controllers = [TestRestController::class], properties = ["spring.main.web-application-type=reactive"])
internal class ReactiveSpaConfigurationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `returns index html for unknown paths`(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) {
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
    fun `returns static resources other than index html`(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) {
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

    @Nested
    inner class `react-scripts project cache control` {

        @TestFactory
        @DisplayName("cacheable resources")
        fun cacheableResources(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) = listOf(
            "static/static/css/main.073c9b0a.css",
            "static/static/js/main.ab3575d7.js",
            "static/static/media/logo.6ce24c58023cc2f8fd88fe9d219db6c6.svg",
        ).map { fileName ->
            file(fileName)
            DynamicTest.dynamicTest(fileName) {
                webTestClient
                    .get()
                    .uri(fileName.replaceFirst("static", ""))
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            }
        }

        @TestFactory
        @DisplayName("non-cacheable resources")
        fun nonCacheableResources(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) = listOf(
            "static/index.html",
            "static/favicon.ico",
            "static/manifest.json",
            "static/logo.png",
        ).map { fileName ->
            file(fileName)
            DynamicTest.dynamicTest(fileName) {
                webTestClient
                    .get()
                    .uri(fileName.replaceFirst("static", ""))
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .doesNotExist("Cache-Control")
            }
        }
    }

    @Test
    fun `REST endpoints remain accessible`(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) {
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
}
