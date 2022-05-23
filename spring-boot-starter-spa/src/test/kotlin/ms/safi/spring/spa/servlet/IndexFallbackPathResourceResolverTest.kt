package ms.safi.spring.spa.servlet

import io.mockk.mockk
import ms.safi.spring.spa.util.files.FileBuilder
import ms.safi.spring.spa.util.files.junit.TempFileBuilder
import ms.safi.spring.spa.util.files.junit.TemporaryFileBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.ClassPathResource

@ExtendWith(TemporaryFileBuilder::class)
internal class IndexFallbackPathResourceResolverTest {

    private val subject = IndexFallbackPathResourceResolver()

    @Test
    fun `resolves existing resources`(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) {
        file("main.css", """I'm a sample main.css""")

        val location = ClassPathResource("/")
        val requestPath = "/main.css"
        val actual = subject.resolveResource(null, requestPath, mutableListOf(location), mockk())

        assertThat(actual).isEqualTo(location.createRelative(requestPath))
    }

    @Test
    fun `resolves index html if requested resource not found`(@TempFileBuilder(rootOnClasspath = true) file: FileBuilder) {
        file("index.html", """I'm a sample index.html file""")

        val location = ClassPathResource("/")
        val requestPath = "/main.css"
        val actual = subject.resolveResource(null, requestPath, mutableListOf(location), mockk())

        assertThat(actual).isEqualTo(location.createRelative("/index.html"))
    }
}