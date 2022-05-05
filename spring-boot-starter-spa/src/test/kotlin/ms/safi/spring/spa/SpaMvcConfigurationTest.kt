package ms.safi.spring.spa

import ms.safi.spring.spa.util.files.FileBuilder
import ms.safi.spring.spa.util.files.junit.TempFileBuilder
import ms.safi.spring.spa.util.files.junit.TemporaryFileBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExtendWith(TemporaryFileBuilder::class)
@WebMvcTest(controllers = [TestRestController::class])
internal class SpaMvcConfigurationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `returns index html for unknown paths`(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) {
        file("static/index.html", """I'm a sample index.html""")

        mockMvc.get("/unknown").andExpect {
            status { isOk() }
            content { string("""I'm a sample index.html""") }
        }

        mockMvc.get("/api/unknown").andExpect {
            status { isOk() }
            content { string("""I'm a sample index.html""") }
        }
    }

    @Test
    fun `returns static resources other than index html`(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) {
        file("static/example.txt", """I'm a sample text file""")
        file("static/static/another.txt", """I'm another text file""")

        mockMvc.get("/example.txt").andExpect {
            status { isOk() }
            content { string("""I'm a sample text file""") }
        }

        mockMvc.get("/static/another.txt").andExpect {
            status { isOk() }
            content { string("""I'm another text file""") }
        }
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
                mockMvc.get(fileName.replaceFirst("static", "")).andExpect {
                    status { isOk() }
                    header {
                        string("Cache-Control", "max-age=31536000")
                    }
                }
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
                mockMvc.get(fileName.replaceFirst("static", "")).andExpect {
                    status { isOk() }
                    header {
                        doesNotExist("Cache-Control")
                    }
                }
            }
        }
    }

    @Test
    fun `REST endpoints remain accessible`(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) {
        file("test", "file that should not be returned")

        mockMvc.get("/test").andExpect {
            status { isOk() }
            content { json("""{"foo": "bar"}""") }
        }
    }
}
